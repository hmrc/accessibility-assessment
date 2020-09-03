FROM selenium/node-chrome:3.141.59-20200826

ENV CAPTURE_ALL_PAGES false
ENV APP_PORT 6010
ENV NVM_VERSION 0.35.3
ENV NODE_VERSION 12.16.1
ENV NPM_AXE_CLI_VERSION 3.2.0
ENV NPM_VNU_VERSION 20.5.29
ENV SCALA_VERSION 2.11.8
ENV NVM_DIR $HOME/.nvm
ENV PATH="${NVM_DIR}/versions/node/v${NODE_VERSION}/bin:${PATH}"

USER root

WORKDIR ${HOME}

COPY --chown=seluser:seluser files/start.sh ${HOME}/start.sh
COPY --chown=seluser:seluser files/app ${HOME}/app

RUN sudo chmod 755 ${HOME}/start.sh \
      && sudo chmod -R 755 ${HOME}/app \
      && sudo mkdir ${HOME}/output \
      && sudo chmod 755 ${HOME}/output \
      && sudo chown -R seluser:seluser ${HOME}/output \
      && sudo mkdir $NVM_DIR \
      && sudo chown seluser:seluser $NVM_DIR \
      && sudo echo -e '#!/bin/bash\njava -jar ${HOME}/.nvm/versions/node/v${NODE_VERSION}/lib/node_modules/vnu-jar/build/dist/vnu.jar "$@"' > /usr/bin/vnu \
      && chmod +x /usr/bin/vnu

USER seluser

RUN   cd ${HOME} \
      && echo "${NODE_VERSION}" > .nvmrc \
      && mv .nvmrc .nvmrc.bk \
      && curl -o- https://raw.githubusercontent.com/creationix/nvm/v${NVM_VERSION}/install.sh | bash \
      && . "${NVM_DIR}/nvm.sh" \
      && mv .nvmrc.bk .nvmrc \
      && nvm install \
      && npm upgrade \
      && npm install axe-cli@${NPM_AXE_CLI_VERSION} -g \
      && cd ${HOME} \
      && npm install vnu-jar@${NPM_VNU_VERSION} -g \
      && cd ${HOME}/app \
      && npm install

EXPOSE 6010

CMD ["/bin/bash", "/home/seluser/start.sh"]
