# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #
#                                                                              #
# Copyright © 2021-2021 Thales SIX GTS FRANCE                                  #
#                                                                              #
# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #


import setuptools

description = """Theresis environment library for reinforcement learning."""

setuptools.setup(
    name='iv4xrl',
    version='0.0.1',
    description='iv4XR RL utilities',
    long_description=description,
    author='Kazmierowski Alexandre',
    author_email='alexandre.kazmierowski@thalesgroup.com',
    license='MIT License',
    keywords='Thales, Reinforcement Learning, iv4XR',
    url='https://www.thales.com',
    packages=setuptools.find_packages(),
    install_requires=[
        'pyzmq>=22.3.0',
        'gym>=0.26.2',
        'coloredlogs>=15.0.1'
    ]
)
