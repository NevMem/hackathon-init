import argparse


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('config_file')
    parser.add_argument('out_dir')
    return parser.parse_args()


def main():
    print(parse_args())


if __name__ == '__main__':
    main()
