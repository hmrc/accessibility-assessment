ARG DOCKERHUB=dockerhub.tax.service.gov.uk

FROM amazonlinux as accessibility-assessment
# https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html#Red+Hat+Enterprise+Linux+and+other+RPM-based+distributions
RUN yum install -y java-1.8.0-openjdk-devel
RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > /etc/yum.repos.d/sbt-rpm.repo
RUN yum install -y sbt git
COPY files/accessibility-assessment /accessibility-assessment
WORKDIR /accessibility-assessment
RUN git init .
RUN git config --global user.email "you@example.com"
RUN git config --global user.name "Your Name"
RUN git add .
RUN git commit -m "satisfy autobuild plugin"
RUN sbt clean assembly

FROM ${DOCKERHUB}/selenium/node-chrome:3.141.59-20201119

ENV CAPTURE_ALL_PAGES false
ENV APP_PORT 6010
ENV NVM_VERSION 0.35.3
ENV NODE_VERSION 12.16.1
ENV SCALA_VERSION 2.12.12
ENV NVM_DIR $HOME/.nvm
ENV PATH="${NVM_DIR}/versions/node/v${NODE_VERSION}/bin:${HOME}/bin:${PATH}"

USER root

WORKDIR ${HOME}

COPY --chown=seluser:seluser files/start.sh ${HOME}/start.sh
COPY --chown=seluser:seluser files/accessibility-assessment/accessibility-assessment-service ${HOME}/accessibility-assessment-service
COPY --chown=seluser:seluser --from=accessibility-assessment /accessibility-assessment/accessibility-assessment-service/app/resources/page-accessibility-check.jar \
      ${HOME}/accessibility-assessment-service/app/resources/page-accessibility-check.jar

RUN sudo chmod 755 ${HOME}/start.sh \
      && sudo chmod -R 755 ${HOME}/accessibility-assessment-service \
      && sudo mkdir ${HOME}/output \
      && sudo chmod 755 ${HOME}/output \
      && sudo chown -R seluser:seluser ${HOME}/output \
      && sudo mkdir $NVM_DIR \
      && sudo chown seluser:seluser $NVM_DIR \
      && sudo mkdir ${HOME}/bin \
      && sudo chown seluser:seluser ${HOME}/bin

USER seluser

RUN   cd ${HOME} \
      && echo "${NODE_VERSION}" > .nvmrc \
      && mv .nvmrc .nvmrc.bk \
      && curl -o- https://raw.githubusercontent.com/creationix/nvm/v${NVM_VERSION}/install.sh | bash \
      && . "${NVM_DIR}/nvm.sh" \
      && mv .nvmrc.bk .nvmrc \
      && nvm install \
      && npm upgrade \
      && cd ${HOME}/accessibility-assessment-service \
      # installs axe, vnu and dependencies required for accessibility-assessment-service defined in package.json
      && npm install \
      # creates a shell script in ${HOME}/bin to make axe and vnu accessible from command line \
      # The name of the shell script should match the invocation from page-accessibility-check.
      && sudo echo -e '#!/bin/bash\njava -jar ${HOME}/accessibility-assessment-service/node_modules/vnu-jar/build/dist/vnu.jar "$@"' > ${HOME}/bin/vnu \
      && sudo echo -e '#!/bin/bash\n cd ${HOME}/accessibility-assessment-service;npx axe "$@"' > ${HOME}/bin/axeRunner \
      # makes axe and vnu shell scripts executable
      && chmod +x ${HOME}/bin/vnu \
      && chmod +x ${HOME}/bin/axeRunner \
      ## runs npm test for accessibility-assessment service when building the image. \
      ## --runInBand disables running test in parallel.
      && npm run test -- --runInBand

EXPOSE 6010

CMD ["/bin/bash", "/home/seluser/start.sh"]
