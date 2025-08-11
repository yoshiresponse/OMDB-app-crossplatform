//
//  omdb_swiftApp.swift
//  omdb-swift
//
//  Created by Yoshi Martodihardjo on 11/08/2025.
//

import SwiftUI

@main
struct omdb_swiftApp: App {
    @StateObject private var favorites = FavoritesStore()
    var body: some Scene {
        WindowGroup {
            TabView {
                ContentView()
                    .tabItem { Label("Search", systemImage: "magnifyingglass") }
                FavoritesView()
                    .tabItem { Label("Favorites", systemImage: "heart") }
                ChatView()
                    .tabItem { Label("Chat", systemImage: "text.bubble") }
            }
            .environmentObject(favorites)
        }
    }
}
