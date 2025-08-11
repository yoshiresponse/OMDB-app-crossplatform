import SwiftUI

struct ChatView: View {
    @StateObject private var vm = ChatViewModel()
    @EnvironmentObject private var favorites: FavoritesStore

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                HStack {
                    TextField("Ask about movies...", text: $vm.input)
                        .textFieldStyle(.roundedBorder)
                        .submitLabel(.send)
                        .onSubmit { Task { await vm.send() } }
                    Button("Send") { Task { await vm.send() } }
                        .buttonStyle(.borderedProminent)
                }

                if vm.loading { ProgressView() }
                if let e = vm.error { Text(e).foregroundColor(.red) }

                if !vm.natural.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Gemini says:")
                            .font(.headline)
                        Text(vm.natural)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                if !vm.suggestions.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(vm.suggestions, id: \.self) { s in
                                Button(action: { Task { await vm.runSuggestion(s) } }) {
                                    Text(s)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 8)
                                        .background(Color.accentColor.opacity(0.15))
                                        .clipShape(Capsule())
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }

                List(vm.results) { movie in
                    NavigationLink(value: movie) {
                        HStack(spacing: 12) {
                            AsyncImage(url: URL(string: movie.poster)) { phase in
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
                                Text(movie.title).font(.headline)
                                Text(movie.year).font(.subheadline).foregroundColor(.secondary)
                                Text(movie.type.capitalized).font(.caption).foregroundColor(.secondary)
                            }

                            Spacer()
                            Button(action: { favorites.toggle(movie.imdbID) }) {
                                Image(systemName: favorites.isFavorite(movie.imdbID) ? "heart.fill" : "heart")
                                    .foregroundColor(.red)
                            }
                            .buttonStyle(.plain)
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
            .navigationTitle("Chat")
        }
    }
}

#Preview {
    ChatView().environmentObject(FavoritesStore())
}
