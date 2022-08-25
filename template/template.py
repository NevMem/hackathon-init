from abc import ABC, abstractclassmethod
from config.config_file import ConfigFile
from typing import List, Dict
import jinja2
import json
import os
import subprocess as sp
from termcolor import cprint


class TemplateFile:
    filename: str
    path: str

    def __init__(self, filename: str, path: str):
        self.filename = filename
        self.path = path

    def run(self, out_dir: str, *args, **kwargs):
        out_path = os.path.join(out_dir, self._get_output_path())
        out_path = jinja2.Template(out_path).render(*args, **kwargs)
        self._ensure_dirs_exists(out_path)
        if self._should_use_jinja():
            with open(self.filename, 'r') as inp:
                temp = inp.read()
            template = jinja2.Template(temp)
            with open(out_path, 'w') as out:
                out.write(template.render(*args, **kwargs))
        else:
            with open(self.filename, 'rb') as inp:
                with open(out_path, 'wb') as out:
                    out.write(inp.read())


    @staticmethod
    def _ensure_dirs_exists(to_file: str):
        os.makedirs('/'.join(to_file.split('/')[:-1]), exist_ok=True)

    def _should_use_jinja(self):
        if '.jinja' in self.filename:
            return True
        return False

    def _get_output_path(self) -> str:
        return os.path.join(self.path, self.filename.split('/')[-1].replace('.jinja', ''))

    def __repr__(self) -> str:
        return str(self.__dict__)


class TemplateTest(ABC):
    @abstractclassmethod
    def run(self, out_dir: str):
        pass


class TemplateGradleTest(TemplateTest):
    command: str
    name: str
    def __init__(self, command: str, name: str):
        self.command = command
        self.name = name

    def run(self, out_dir: str):
        cprint(f"Running test: {self.name}", color='green')
        try:
            sp.check_output(['/bin/bash', './gradlew', self.command], cwd=out_dir)
        except sp.CalledProcessError as error:
            print(error.returncode)

    def __repr__(self):
        return str(self.__dict__)


class Template:
    files: List[TemplateFile]
    depends_on: List[str]
    defines: Dict[str, str]
    template_id: str
    marker: str
    tests: List[TemplateTest]

    def __init__(self, template_id: str, marker: str, files: List[TemplateFile], depends_on: List[str], defines: Dict[str, str], tests: List[TemplateTest]):
        self.template_id = template_id
        self.files = files
        self.marker = marker
        self.depends_on = depends_on
        self.defines = defines
        self.tests = tests

    def run(self, out_dir: str, *args, **kwargs):
        cprint(f"Running template: {self.template_id.upper()}", on_color='on_green')
        for file in self.files:
            file.run(out_dir, *args, **kwargs)

    def run_tests(self, out_dir: str):
        if len(self.tests) == 0:
            return
        cprint(f"Running tests for template {self.template_id.upper()}:", on_color='on_green')
        for test in self.tests:
            test.run(out_dir)

    def __repr__(self):
        return str(self.__dict__)


class TemplateLoader:
    @staticmethod
    def load(dirname: str) -> Template:
        with open(os.path.join(dirname, 'template.json'), 'r') as inp:
            data = json.loads(inp.read())

            files = []

            for file in data['files']:
                files.append(TemplateFile(filename=os.path.join(dirname, file['file']), path=file['path']))

            defines = dict()
            if 'defines' in data:
                defines = data['defines']

            tests = []
            if 'tests' in data:
                for test in data['tests']:
                    if 'type' not in test:
                        cprint(f"Malformed test found: {test}", on_color='on_red')
                        continue
                    if test['type'] == 'gradle':
                        tests.append(TemplateGradleTest(test['command'], test['name']))

            depends = []
            if 'depends_on' in data:
                depends = data['depends_on']

            return Template(
                template_id=data['id'],
                marker=data['marker'],
                files=files,
                depends_on=depends,
                defines=defines,
                tests=tests,
            )


    @staticmethod
    def load_all_from_dir(dirname: str) -> List[Template]:
        result = []
        for entity in os.listdir(dirname):
            joined = os.path.join(dirname, entity)
            if os.path.isdir(joined) and os.path.exists(os.path.join(joined, 'template.json')):
                result.append(TemplateLoader.load(joined))
        return result
