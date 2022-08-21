import argparse

from config.config_file import ConfigFileParser


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('config_file')
    parser.add_argument('out_dir')
    return parser.parse_args()


def main():
    args = parse_args()
    config = ConfigFileParser.parse(args.config_file)
    print(config)


if __name__ == '__main__':
    main()
