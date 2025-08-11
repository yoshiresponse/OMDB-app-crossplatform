//
//  ContentView.swift
//  omdb-swift
//
//  Created by Yoshi Martodihardjo on 11/08/2025.
//

import SwiftUI

struct ContentView: View {
    @State private var query: String = ""
    @State private var loading = false
    @State private var error: String?
    @State private var results: [MovieShort] = []

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                HStack {
                    TextField("Search movies", text: $query)
                        .textFieldStyle(.roundedBorder)
                        .submitLabel(.search)
                        .onSubmit { Task { await search() } }
                    Button("Search") { Task { await search() } }
                        .buttonStyle(.borderedProminent)
                }

                if loading { ProgressView().frame(maxWidth: .infinity, alignment: .leading) }
                if let e = error { Text(e).foregroundColor(.red) }

                List(results) { movie in
                    NavigationLink(value: movie) {
                        HStack(alignment: .top, spacing: 12) {
                            AsyncImage(url: URL(string: movie.poster)) { phase in
                                switch phase {
                                case .success(let image):
                                    image.resizable().scaledToFill()
                                case .failure(_):
                                    Color.gray.opacity(0.2)
                                case .empty:
                                    ProgressView()
                                @unknown default:
                                    Color.gray.opacity(0.2)
                                }
                            }
                            .frame(width: 60, height: 90)
                            .clipShape(RoundedRectangle(cornerRadius: 8))

                            VStack(alignment: .leading, spacing: 4) {
                                Text(movie.title).font(.headline)
                                Text(movie.year).font(.subheadline).foregroundColor(.secondary)
                                Text(movie.type.capitalized).font(.caption).foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .listStyle(.plain)
                .navigationDestination(for: MovieShort.self) { movie in
                    DetailsView(imdbID: movie.imdbID)
                }

                Spacer(minLength: 0)
            }
            .padding()
            .navigationTitle("OMDb Search")
        }
    }

    private func search() async {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return }
        loading = true
        error = nil
        do {
            let list = try await OMDBAPI.shared.search(query: q)
            await MainActor.run {
                self.results = list
            }
        } catch {
            await MainActor.run {
                self.error = error.localizedDescription
            }
        }
        await MainActor.run { self.loading = false }
    }
}

#Preview {
    ContentView()
}
