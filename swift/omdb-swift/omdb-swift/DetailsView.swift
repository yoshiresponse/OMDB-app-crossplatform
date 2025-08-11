import SwiftUI

struct DetailsView: View {
    let imdbID: String

    @State private var detail: MovieDetail?
    @State private var loading = true
    @State private var error: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if loading {
                    ProgressView("Loading...")
                        .frame(maxWidth: .infinity, alignment: .center)
                }

                if let e = error {
                    Text(e)
                        .foregroundColor(.red)
                }

                if let d = detail {
                    if let poster = d.poster, let url = URL(string: poster) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .clipShape(RoundedRectangle(cornerRadius: 12))
                            case .failure:
                                Color.gray.opacity(0.2).frame(height: 200)
                            case .empty:
                                ProgressView().frame(height: 200)
                            @unknown default:
                                EmptyView()
                            }
                        }
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text(d.title ?? "Unknown Title")
                            .font(.title2).bold()
                        HStack(spacing: 12) {
                            if let year = d.year, !year.isEmpty {
                                Label(year, systemImage: "calendar")
                            }
                            if let type = d.type, !type.isEmpty {
                                Label(type.capitalized, systemImage: "film")
                            }
                            if let rating = d.imdbRating, !rating.isEmpty {
                                Label("IMDb " + rating, systemImage: "star.fill")
                            }
                        }
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    }

                    if let plot = d.plot, !plot.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Plot").font(.headline)
                            Text(plot)
                        }
                    }

                    if let genre = d.genre, !genre.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Genre").font(.headline)
                            Text(genre)
                        }
                    }

                    Spacer(minLength: 0)
                }
            }
            .padding()
        }
        .navigationTitle("Details")
        .task { await load() }
    }

    private func load() async {
        loading = true
        error = nil
        do {
            let d = try await OMDBAPI.shared.details(id: imdbID)
            await MainActor.run { self.detail = d }
        } catch {
            await MainActor.run { self.error = error.localizedDescription }
        }
        await MainActor.run { self.loading = false }
    }
}

#Preview {
    NavigationStack {
        DetailsView(imdbID: "tt0133093")
    }
}
