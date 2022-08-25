import argparse
import sys
from typing import List, Dict
from termcolor import cprint

from config.config_file import ConfigFile, ConfigFileParser
from template.template import Template, TemplateLoader


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('config_file')
    parser.add_argument('out_dir')
    parser.add_argument('--run-tests', action='store_true')
    return parser.parse_args()


def template_with_id(templates: List[Template], template_id: str) -> Template:
    for template in templates:
        if template.template_id == template_id:
            return template
    cprint(f"Cannot find template with id: {template_id}", on_color='on_red')
    sys.exit(1)


def get_selected_templates(templates: List[Template], markers: List[str]) -> List[Template]:
    selected: List[Template] = []
    selected_ids = set()
    for template in templates:
        if template.marker in markers:
            selected.append(template)
            selected_ids.add(template.template_id)
    

    while True:
        added = False
        for template in selected:
            for depends in template.depends_on:
                if depends not in selected_ids:
                    new_template = template_with_id(templates=templates, template_id=depends)
                    selected_ids.add(depends)
                    selected.append(new_template)
                    added = True
        if not added:
            break

    for marker in markers:
        found = False
        for template in selected:
            if template.marker == marker:
                found = True
        if not found:
            cprint(f"Template with marker {marker} do not exists", on_color='on_red')
            sys.exit(1)

    return selected


class Pipeline:
    confif: ConfigFile
    templates: List[Template]
    args: Dict[str, str]
    def __init__(self, args: Dict[str, str], config: ConfigFile, templates: List[Template]):
        self.args = args
        self.config = config
        self.templates = templates

    def run(self):
        cprint(f"Loading markers from config file", on_color='on_cyan')
        markers = self.config.get_markers()
        cprint(f"Markers are: {', '.join(markers)}", on_color='on_green')

        selected_templates = get_selected_templates(templates=self.templates, markers=markers)

        global_defines = self._create_global_defines(selected_templates)

        execution_order = self._create_execution_order(selected_templates)
        cprint(f"Calculated templates execution order: {', '.join(map(lambda x: x.template_id, execution_order))}", on_color='on_green')
        
        for template in execution_order:
            template.run(self.args.out_dir, config=self.config, **global_defines)

        if self.args.run_tests:
            for template in execution_order:
                template.run_tests(self.args.out_dir)

    @staticmethod
    def _create_execution_order(templates: List[Template]) -> List[Template]:
        ordered_template_ids = set()
        ordered_templates: List[Template] = []

        # Sorry for this, but this is dummy topological sort (just believe me)
        while True:
            added = False
            for template in templates:
                if template.template_id in ordered_template_ids:
                    continue
                all_deps_added = True
                for depends in template.depends_on:
                    if depends not in ordered_template_ids:
                        all_deps_added = False
                if all_deps_added:
                    added = True
                    ordered_template_ids.add(template.template_id)
                    ordered_templates.append(template)
            if not added:
                cprint(f"Check your templates dependencies graph, maybe there are some cycles", on_color='on_red')
                sys.exit(1)
            if len(ordered_template_ids) == len(templates):
                break

        return ordered_templates

    @staticmethod
    def _create_global_defines(templates: List[Template]) -> Dict:
        defines = dict()
        for template in templates:
            for key in template.defines:
                if type(template.defines[key]) == list:
                    if key not in defines:
                        defines[key] = []
                    for value in template.defines[key]:
                        defines[key].append(value)
                else:
                    defines[key] = template.defines[key]
        return defines


def main():
    args = parse_args()
    config = ConfigFileParser.parse(args.config_file)

    # base_template.run(args.out_dir, config=config)

    templates = TemplateLoader.load_all_from_dir('templates')

    for template in templates:
        cprint(f"Loaded template: {template.template_id}", on_color='on_green')

    pipeline = Pipeline(
        args=args,
        config=config,
        templates=templates,
    )

    pipeline.run()


if __name__ == '__main__':
    main()
