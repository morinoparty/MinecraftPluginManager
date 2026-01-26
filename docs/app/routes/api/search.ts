import { createFileRoute } from "@tanstack/react-router";
import { createFromSource } from "fumadocs-core/search/server";
import { source } from "../../lib/source";

const search = createFromSource(source);

export const Route = createFileRoute("/api/search")({
    server: {
        handlers: {
            GET: async ({ request }) => {
                const url = new URL(request.url);
                const query = url.searchParams.get("query") ?? "";

                const results = await search.search(query);

                return new Response(JSON.stringify(results), {
                    headers: {
                        "Content-Type": "application/json",
                    },
                });
            },
        },
    },
});
