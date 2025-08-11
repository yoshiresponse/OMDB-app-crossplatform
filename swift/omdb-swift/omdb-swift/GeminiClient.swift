import Foundation

private struct GLRequest: Encodable {
    struct Content: Encodable { let role: String?; let parts: [Part] }
    struct Part: Encodable { let text: String }
    let contents: [Content]
}

private struct GLResponse: Decodable {
    struct Candidate: Decodable {
        struct Content: Decodable { let parts: [Part] }
        struct Part: Decodable { let text: String? }
        let content: Content?
    }
    let candidates: [Candidate]?
}

final class GeminiClient {
    static let shared = GeminiClient()

    private let session: URLSession
    private let apiKey: String

    init(session: URLSession = .shared, apiKey: String = APIKeyManager.shared.geminiApiKey) {
        self.session = session
        self.apiKey = apiKey
    }

    func respondAndSuggest(prompt: String) async throws -> (natural: String, queries: [String]) {
        guard !apiKey.isEmpty else { throw NSError(domain: "Gemini", code: -1, userInfo: [NSLocalizedDescriptionKey: "Missing GEMINI_API_KEY"]) }

        // Single-call prompt to get natural response and structured suggestions.
        // We instruct the model to output with a delimiter line.
        let instruction = """
        You are helping users find movies via the OMDb API. First, reply concisely to the user's message. Then output a section named SUGGESTIONS with up to 3 short, concise OMDb search queries, one per line, no numbering.
        Format:
        <natural response>
        \nSUGGESTIONS:
        <query 1>
        <query 2>
        <query 3>
        """.trimmingCharacters(in: .whitespacesAndNewlines)
        let userPrompt = "User: \(prompt)"

        let req = GLRequest(contents: [
            .init(role: "user", parts: [.init(text: instruction), .init(text: userPrompt)])
        ])

        let model = "gemini-1.5-flash"
        let urlStr = "https://generativelanguage.googleapis.com/v1beta/models/\(model):generateContent?key=\(apiKey)"
        guard let url = URL(string: urlStr) else { throw URLError(.badURL) }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(req)

        do {
            let (data, _) = try await session.data(for: request)
            let decoded = try JSONDecoder().decode(GLResponse.self, from: data)
            // Join all returned parts for robustness
            let text = (decoded.candidates?.first?.content?.parts.compactMap { $0.text }.joined(separator: "\n")) ?? ""
            let (natural, queries) = parseOutput(text)
            // Fallback suggestions if parsing fails
            let qs = queries.isEmpty ? defaultSuggestions(from: prompt) : queries
            return (natural: natural.isEmpty ? "" : natural, queries: Array(qs.prefix(3)))
        } catch {
            // Network or decode failure: return heuristic suggestions
            return (natural: "Here are some ideas based on your prompt: \(prompt)",
                    queries: Array(defaultSuggestions(from: prompt).prefix(3)))
        }
    }

    private func parseOutput(_ text: String) -> (String, [String]) {
        let delimiter = "SUGGESTIONS:"
        if let range = text.range(of: delimiter) {
            let natural = String(text[..<range.lowerBound]).trimmingCharacters(in: .whitespacesAndNewlines)
            let rest = String(text[range.upperBound...]).trimmingCharacters(in: .whitespacesAndNewlines)
            let lines = rest.replacingOccurrences(of: "\r", with: "")
            let queries = lines
                .split(separator: "\n")
                .map { $0.replacingOccurrences(of: "- ", with: "").replacingOccurrences(of: "• ", with: "").trimmingCharacters(in: .whitespacesAndNewlines) }
                .filter { !$0.isEmpty }
            return (natural, queries)
        }
        return (text.trimmingCharacters(in: .whitespacesAndNewlines), [])
    }

    private func defaultSuggestions(from prompt: String) -> [String] {
        let base = prompt.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !base.isEmpty else { return [] }
        return [base, "best \(base) movies", "top rated \(base)"]
    }
}
