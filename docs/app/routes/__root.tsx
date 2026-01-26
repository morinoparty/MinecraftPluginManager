import { createRootRoute, HeadContent, Outlet, Scripts, useLocation } from "@tanstack/react-router";
import { RootProvider } from "fumadocs-ui/provider/tanstack";
import appCss from "../app.css?url";

export const Route = createRootRoute({
    head: () => ({
        meta: [
            { charSet: "utf-8" },
            { name: "viewport", content: "width=device-width, initial-scale=1" },
            { name: "description", content: "MPM - MinecraftPluginManager Documentation" },
        ],
        links: [
            { rel: "icon", type: "image/svg+xml", href: "/favicon.svg" },
            { rel: "stylesheet", href: appCss },
            { rel: "stylesheet", href: "https://api.fontshare.com/v2/css?f[]=satoshi@1&display=swap" },
            {
                rel: "stylesheet",
                type: "text/css",
                href: "https://shogo82148.github.io/genjyuugothic-subsets/GenJyuuGothicL-P-Medium/GenJyuuGothicL-P-Medium.css",
            },
            {
                rel: "stylesheet",
                type: "text/css",
                href: "https://shogo82148.github.io/genjyuugothic-subsets/GenJyuuGothicL-P-Bold/GenJyuuGothicL-P-Bold.css",
            },
        ],
    }),
    component: RootComponent,
});

function RootComponent() {
    const location = useLocation();
    const isDocsPage = location.pathname.startsWith("/docs");

    return (
        <html lang="ja">
            <head>
                <HeadContent />
            </head>
            <body className="m-0 min-h-screen flex flex-col bg-fd-background text-fd-foreground">
                <RootProvider>
                    <ThemeScript />
                    <main className="flex-1 w-full">
                        <Outlet />
                    </main>
                    <Scripts />
                </RootProvider>
            </body>
        </html>
    );
}

function ThemeScript() {
    return (
        <script
            // biome-ignore lint/security/noDangerouslySetInnerHtml: SSR theme initialization script
            dangerouslySetInnerHTML={{
                __html: `
          (function() {
            const mode = localStorage.getItem('theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
            document.documentElement.classList.toggle('dark', mode === 'dark');
          })();
        `,
            }}
        />
    );
}
