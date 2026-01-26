import { loader } from "fumadocs-core/source";
import { toFumadocsSource } from "fumadocs-mdx/runtime/server";
import { icons } from "lucide-react";
import { createElement } from "react";
import { docs, meta } from "../../.source/server";

export const source = loader({
    baseUrl: "/docs",
    source: toFumadocsSource(docs, meta),
    icon(icon) {
        if (!icon) return;
        if (icon in icons) return createElement(icons[icon as keyof typeof icons]);
    },
});
