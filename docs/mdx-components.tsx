import type { MDXComponents } from "mdx/types";
import defaultComponents from "fumadocs-ui/mdx";
import { CommandList } from "./app/components/command-list";
import { RepoFileGenerator } from "./app/components/repo-file-generator";

const customComponents = {
    CommandList,
    RepoFileGenerator,
};

export function getMDXComponents(): MDXComponents {
    return customComponents;
}

export function useMDXComponents(components: MDXComponents): MDXComponents {
    return {
        ...defaultComponents,
        ...components,
        ...customComponents,
    };
}
