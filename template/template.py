from config.config_file import ConfigFile
from typing import List
import jinja2
import json
import os


class TemplateFile:
    filename: str
    path: str

    def __init__(self, filename: str, path: str):
        self.filename = filename
        self.path = path

    def run(self, out_dir: str, *args, **kwargs):
        with open(self.filename, 'r') as inp:
            temp = inp.read()
        template = jinja2.Template(temp)
        with open(os.path.join(out_dir, self._get_output_path()), 'w') as out:
            out.write(
                template.render(*args, **kwargs)
            )

    def _get_output_path(self) -> str:
        return os.path.join(self.path, self.filename.split('/')[-1].replace('.jinja', ''))

    def __repr__(self) -> str:
        return str(self.__dict__)


class Template:
    files: List[TemplateFile]

    def __init__(self, files: List[TemplateFile]):
        self.files = files

    def run(self, out_dir: str, *args, **kwargs):
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

            return Template(files=files)
