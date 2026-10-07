import UIKit
import SwiftUI
import Shared
import UserNotifications

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onAppear {
                requestNotificationPermission()
            }
            .onOpenURL { url in
                DeepLinkManager.shared.setPendingInviteCode(rawUriOrCode: url.absoluteString)
            }
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if granted {
                print("FAIRSPLIT_NOTIF: iOS Notification permission granted")
            } else if let error = error {
                print("FAIRSPLIT_NOTIF: iOS Notification permission error: \(error.localizedDescription)")
            } else {
                print("FAIRSPLIT_NOTIF: iOS Notification permission denied")
            }
        }
    }
}