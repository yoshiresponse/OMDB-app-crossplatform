import SwiftUI

struct FavoritesView: View {
    @EnvironmentObject private var favorites: FavoritesStore
    @State private var loading = false
    @State private var error: String?
    @State private var items: [MovieDetail] = []

    var body: some View {
        NavigationStack {
            Group {
                if loading { ProgressView() }
                if let e = error { Text(e).foregroundColor(.red) }
                List(items, id: \.id) { d in
                    NavigationLink(value: d.imdbID ?? d.id) {
                        HStack(spacing: 12) {
                            AsyncImage(url: URL(string: d.poster ?? "")) { phase in
                                switch phase {
                                case .success(let image): image.resizable().scaledToFill()
                                case .failure(_): Color.gray.opacity(0.2)
                                case .empty: ProgressView()
                                @unknown default: Color.gray.opacity(0.2)
                                }
                            }
                            .frame(width: 60, height: 90)
                            .clipShape(RoundedRectangle(cornerRadius: 8))

                            VStack(alignment: .leading, spacing: 4) {
                                Text(d.title ?? "").font(.headline)
                                Text(d.year ?? "").font(.subheadline).foregroundColor(.secondary)
                            }

                            Spacer()
                            Button(action: { if let id = d.imdbID { favorites.toggle(id) } }) {
                                Image(systemName: "heart.fill").foregroundColor(.red)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .listStyle(.plain)
            }
            .navigationTitle("Favorites")
            .navigationDestination(for: String.self) { imdb in
                DetailsView(imdbID: imdb)
            }
            .task { await reload() }
            .onReceive(favorites.$ids) { _ in Task { await reload() } }
        }
    }

    private func reload() async {
        loading = true
        error = nil
        let ids = Array(favorites.ids)
        var out: [MovieDetail] = []
        for id in ids {
            do { out.append(try await OMDBAPI.shared.details(id: id)) } catch { /* skip */ }
        }
        await MainActor.run { self.items = out }
        await MainActor.run { self.loading = false }
    }
}

#Preview {
    FavoritesView().environmentObject(FavoritesStore())
}
