import yaml
from typing import Optional
from enum import Enum

class AndroidUIType(Enum):
    COMPOSE = 1
    DEFAULT = 2


class AndroidConfig:
    ui_type: AndroidUIType

    def __init__(self, obj):
        self.ui_type = AndroidUIType.DEFAULT
        if 'ui' in obj:
            if obj['ui'] == 'compose':
                self.ui_type = AndroidUIType.COMPOSE

    def __repr__(self):
        return str(self.__dict__)


class ConfigFile:
    android: Optional[AndroidConfig]
    base_package_name: str
    project_name: str

    def __init__(self, obj):
        self.android = None
        if 'android' in obj:
            self.android = AndroidConfig(obj['android'])

        self.base_package_name = obj['base_package_name']
        self.project_name = obj['project_name']

    def __repr__(self):
        return str(self.__dict__)


class ConfigFileParser:
    @staticmethod
    def parse(filename: str) -> ConfigFile:
        with open(filename, 'r') as input_stream:
            return ConfigFile(yaml.safe_load(input_stream))
