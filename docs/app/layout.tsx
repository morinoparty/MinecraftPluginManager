import "./global.css";
import type { Metadata } from "next";
import type { ReactNode } from "react";
import { Provider } from "@/components/provider";

export const metadata: Metadata = {
    title: {
        template: "%s | MPM",
        default: "MinecraftPluginManager Documentation",
    },
    description: "Minecraft plugin manager for easy plugin installation and management",
};

export default function RootLayout({ children }: { children: ReactNode }) {
    return (
        <html lang="ja" suppressHydrationWarning>
            <body className="flex flex-col min-h-screen">
                <Provider>{children}</Provider>
            </body>
        </html>
    );
}
