import Foundation
import Combine

final class FavoritesStore: ObservableObject {
    @Published private(set) var ids: Set<String> = []
    private let defaultsKey = "favorites_ids"
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        if let arr = defaults.array(forKey: defaultsKey) as? [String] {
            self.ids = Set(arr)
        }
    }

    func isFavorite(_ id: String) -> Bool { ids.contains(id) }

    func toggle(_ id: String) {
        var copy = ids
        if copy.contains(id) {
            copy.remove(id)
        } else {
            copy.insert(id)
        }
        ids = copy
        defaults.set(Array(copy), forKey: defaultsKey)
    }
}
