import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import viteReact from "@vitejs/plugin-react";
import fumadocs from "fumadocs-mdx/vite";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";
import * as MdxConfig from "./source.config";

export default defineConfig({
    // 環境変数VITE_BASE_URLでbaseを設定可能（プレビュー環境用）
    base: process.env.VITE_BASE_URL || "/",
    plugins: [
        tailwindcss(),
        fumadocs(MdxConfig, {
            index: {
                target: "vite",
            },
        }),
        tanstackStart({
            srcDirectory: "app",
            prerender: {
                enabled: true,
                crawlLinks: true,
            },
        }),
        tsconfigPaths(),
        viteReact(),
    ],
    resolve: {
        alias: {
            "@components": `${__dirname}/components`,
            "fumadocs-mdx:collections/*": `${__dirname}/.source/*`,
            // Node.js pathモジュールのブラウザ向けポリフィル
            path: "path-browserify",
        },
    },
    server: {
        port: 3010,
        allowedHosts: ["localhost", "127.0.0.1", "0.0.0.0", ".trycloudflare.com"],
    },
});
