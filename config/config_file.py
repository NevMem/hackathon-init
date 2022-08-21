import yaml

class ConfigFile:
    def __init__(self, obj):
        print(obj)


class ConfigFileParser:
    @staticmethod
    def parse(filename: str) -> ConfigFile:
        with open(filename, 'r') as input_stream:
            return ConfigFile(yaml.load(input_stream))
