import Foundation
import SwiftUI

@MainActor
final class ChatViewModel: ObservableObject {
    @Published var input: String = ""
    @Published var natural: String = ""
    @Published var suggestions: [String] = []
    @Published var results: [MovieShort] = []
    @Published var loading = false
    @Published var error: String?

    func send() async {
        let q = input.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return }
        loading = true
        error = nil
        do {
            let (naturalText, queries) = try await GeminiClient.shared.respondAndSuggest(prompt: q)
            self.natural = naturalText
            self.suggestions = Array(queries.prefix(3))
            // Aggregate search results from suggestions similar to Kotlin
            var agg: [MovieShort] = []
            for s in self.suggestions {
                do { let part = try await OMDBAPI.shared.search(query: s); agg.append(contentsOf: part) } catch { /* skip */ }
            }
            self.results = agg
        } catch {
            self.error = error.localizedDescription
        }
        self.loading = false
    }

    func runSuggestion(_ s: String) async {
        input = s
        await send()
    }
}
