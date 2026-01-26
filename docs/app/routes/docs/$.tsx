import browserCollections from "fumadocs-mdx:collections/browser";
import { createFileRoute, notFound } from "@tanstack/react-router";
import { createServerFn } from "@tanstack/react-start";
import { useFumadocsLoader } from "fumadocs-core/source/client";
import defaultMdxComponents from "fumadocs-ui/mdx";
import { DocsLayout } from "../../../components/layout/docs";
import { DocsBody, DocsDescription, DocsPage, DocsTitle } from "../../../components/layout/docs/page";
import { getMDXComponents } from "../../../mdx-components";
import { baseOptions } from "../../lib/layout.shared";
import { source } from "../../lib/source";

export const Route = createFileRoute("/docs/$")({
    component: Page,
    loader: async ({ params }) => {
        const slugs = params._splat?.split("/") ?? [];
        const data = await serverLoader({ data: slugs });
        await clientLoader.preload(data.path);
        return data;
    },
});

const serverLoader = createServerFn({
    method: "GET",
})
    .inputValidator((slugs: string[]) => slugs)
    .handler(async ({ data: slugs }) => {
        const page = source.getPage(slugs);
        if (!page) throw notFound();

        return {
            path: page.path,
            pageTree: await source.serializePageTree(source.getPageTree()),
        };
    });

const clientLoader = browserCollections.docs.createClientLoader({
    component({ toc, frontmatter, default: MDX }) {
        return (
            <DocsPage toc={toc}>
                <DocsTitle>{frontmatter.title}</DocsTitle>
                <DocsDescription>{frontmatter.description}</DocsDescription>
                <DocsBody>
                    <MDX
                        components={{
                            ...defaultMdxComponents,
                            ...getMDXComponents(),
                        }}
                    />
                </DocsBody>
            </DocsPage>
        );
    },
});

function Page() {
    const data = Route.useLoaderData();
    const { pageTree } = useFumadocsLoader(data);
    const Content = clientLoader.getComponent(data.path);

    return (
        <DocsLayout
            {...baseOptions()}
            nav={{
                ...baseOptions().nav,
                enabled: true,
            }}
            tree={pageTree}
            sidebar={{
                collapsible: false,
            }}
        >
            <Content />
        </DocsLayout>
    );
}
