
import Foundation

struct APIKeyManager {
    static let shared = APIKeyManager()

    let apiKey: String

    private init() {
        // 1) Try Config.plist (API_KEY or OMDB_API_KEY)
        var key: String? = nil
        if let url = Bundle.main.url(forResource: "Config", withExtension: "plist"),
           let data = try? Data(contentsOf: url),
           let plist = try? PropertyListSerialization.propertyList(from: data, options: [], format: nil) as? [String: Any] {
            key = (plist["API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            if (key ?? "").isEmpty {
                key = (plist["OMDB_API_KEY"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            }
        }
        // 2) Fallback to Info.plist (OMDB_API_KEY)
        if (key ?? "").isEmpty {
            if let info = Bundle.main.infoDictionary,
               let k = info["OMDB_API_KEY"] as? String,
               !k.isEmpty { key = k }
        }
        // 3) Final fallback: empty string (surface error in UI instead of crashing)
        self.apiKey = key ?? ""
    }
}
