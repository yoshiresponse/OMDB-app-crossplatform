
import Foundation

struct APIKeyManager {
    static let shared = APIKeyManager()

    let omdbApiKey: String
    let geminiApiKey: String

    private init() {
        // 1) Try Config.plist (API_KEY/OMDB_API_KEY and GEMINI_API_KEY)
        var omdb: String? = nil
        var gemini: String? = nil
        if let url = Bundle.main.url(forResource: "Config", withExtension: "plist"),
           let data = try? Data(contentsOf: url),
           let plist = try? PropertyListSerialization.propertyList(from: data, options: [], format: nil) as? [String: Any] {
            omdb = (plist["API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            if (omdb ?? "").isEmpty {
                omdb = (plist["OMDB_API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            }
            gemini = (plist["GEMINI_API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        // 2) Fallback to Info.plist
        if (omdb ?? "").isEmpty || (gemini ?? "").isEmpty {
            if let info = Bundle.main.infoDictionary,
               let kOmdb = (info["OMDB_API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) {
                if !(kOmdb).isEmpty { omdb = kOmdb }
            }
            if let info = Bundle.main.infoDictionary,
               let kGemini = (info["GEMINI_API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) {
                if !(kGemini).isEmpty { gemini = kGemini }
            }
        }
        // 3) Final fallback: empty string (surface error in UI instead of crashing)
        self.omdbApiKey = omdb ?? ""
        self.geminiApiKey = gemini ?? ""
    }
}
