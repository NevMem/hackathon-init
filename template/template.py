from config.config_file import ConfigFile
from typing import List
import jinja2
import json
import os
from termcolor import cprint


class TemplateFile:
    filename: str
    path: str

    def __init__(self, filename: str, path: str):
        self.filename = filename
        self.path = path

    def run(self, out_dir: str, *args, **kwargs):
        out_path = os.path.join(out_dir, self._get_output_path())
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


class Template:
    files: List[TemplateFile]
    name: str

    def __init__(self, name: str, files: List[TemplateFile]):
        self.name = name
        self.files = files

    def run(self, out_dir: str, *args, **kwargs):
        cprint(f"Running template: {self.name.upper()}", on_color='on_green')
        for file in self.files:
            file.run(out_dir, *args, **kwargs)

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

            return Template(name=data['name'], files=files)
