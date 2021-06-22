Docker
======

To build a Docker image with the tag TAG, run the following:

    $ docker build -t TAG .
    $ docker tag arkcase/acm-config-server arkcase/acm-config-server:TAG

To run locally using the docker-compose file, you will need to clone
the .arkcase repo in this directory:

    $ git clone git@gitlab.armedia.com:arkcase/.arkcase
    $ docker-compose up --build

**IMPORTANT**: The docker-compose file is provided only to test that
the Docker image and runs locally. In particular, it should not be
used for:
  - Any production environment
  - Any development environment, including a developer's desktop

For the above, a Kubernetes cluster should be used.
