import Foundation

// MARK: - Models
struct MovieShort: Identifiable, Decodable, Hashable {
    let title: String
    let year: String
    let imdbID: String
    let type: String
    let poster: String

    var id: String { imdbID }

    enum CodingKeys: String, CodingKey {
        case title = "Title"
        case year = "Year"
        case imdbID
        case type = "Type"
        case poster = "Poster"
    }
}

struct SearchResponse: Decodable {
    let Search: [MovieShort]?
    let totalResults: String?
    let Response: String?
    let Error: String?
}

struct MovieDetail: Decodable, Identifiable, Hashable {
    let title: String?
    let year: String?
    let plot: String?
    let poster: String?
    let genre: String?
    let imdbRating: String?
    let imdbID: String?
    let type: String?
    let response: String?
    let error: String?

    enum CodingKeys: String, CodingKey {
        case title = "Title"
        case year = "Year"
        case plot = "Plot"
        case poster = "Poster"
        case genre = "Genre"
        case imdbRating
        case imdbID
        case type = "Type"
        case response = "Response"
        case error = "Error"
    }

    var id: String { imdbID ?? UUID().uuidString }
}

// MARK: - API Client
final class OMDBAPI {
    static let shared = OMDBAPI()

    private let apiKey: String
    private let session: URLSession

    private init(session: URLSession = .shared) {
        self.apiKey = APIKeyManager.shared.omdbApiKey
        self.session = session
    }

    private func baseURL() -> URL { URL(string: "https://www.omdbapi.com/")! }

    func search(query: String) async throws -> [MovieShort] {
        var comps = URLComponents(url: baseURL(), resolvingAgainstBaseURL: false)!
        comps.queryItems = [
            URLQueryItem(name: "apikey", value: apiKey),
            URLQueryItem(name: "s", value: query)
        ]
        guard let url = comps.url else { throw URLError(.badURL) }
        let (data, _) = try await session.data(from: url)
        let decoded = try JSONDecoder().decode(SearchResponse.self, from: data)
        if decoded.Response == "True" {
            return decoded.Search ?? []
        } else {
            return []
        }
    }

    func details(id: String) async throws -> MovieDetail {
        var comps = URLComponents(url: baseURL(), resolvingAgainstBaseURL: false)!
        comps.queryItems = [
            URLQueryItem(name: "apikey", value: apiKey),
            URLQueryItem(name: "i", value: id),
            URLQueryItem(name: "plot", value: "short")
        ]
        guard let url = comps.url else { throw URLError(.badURL) }
        let (data, _) = try await session.data(from: url)
        let decoded = try JSONDecoder().decode(MovieDetail.self, from: data)
        if decoded.response == "True" || decoded.imdbID != nil {
            return decoded
        } else {
            throw NSError(domain: "OMDb", code: -1, userInfo: [NSLocalizedDescriptionKey: decoded.error ?? "Unknown error"]) }
    }
}
