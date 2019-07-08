package com.armedia.acm.configserver.service;

/**
 * @author ivana.shekerova on 6/27/2019.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IncludeFilesInFileService {

    private static String basePropertiesPath;
    private static String propertiesIncludeFilesPath;
    private static String mergePropertiesPath;

    private static final Constructor constructor = new MyConstructor();
    private static final Logger logger = LoggerFactory.getLogger(IncludeFilesInFileService.class);

    public IncludeFilesInFileService(@Value("${properties.base.path}") String basePropertiesPath,
                                     @Value("${properties.merge.path}") String mergePropertiesPath,
                                     @Value("${properties.includefiles.path}") String propertiesIncludeFilesPath) {
        this.basePropertiesPath = basePropertiesPath;
        this.mergePropertiesPath = mergePropertiesPath;
        this.propertiesIncludeFilesPath = propertiesIncludeFilesPath;
        logger.debug("Initializing IncludeFilesInFileService");
    }

    private static class ImportConstruct extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            if (!(node instanceof ScalarNode)) {
                throw new IllegalArgumentException("Non-scalar !import: " + node.toString());
            }

            final ScalarNode scalarNode = (ScalarNode) node;
            final String value = scalarNode.getValue();

            File file = new File(propertiesIncludeFilesPath + value);
            if (!file.exists()) {
                return null;
            }

            try {
                final InputStream input = new FileInputStream(new File(propertiesIncludeFilesPath + value));
                final Yaml yaml = new Yaml(constructor);

                Map<String, Object> configMap = yaml.load(input);
                if (configMap == null) {
                    configMap = new LinkedHashMap<>();
                }

                return configMap;
            } catch (IOException e) {
                logger.error("Failed loading one of the configuration files.", e.getMessage());
                logger.trace("Cause: ", e);
            }
            return null;
        }
    }

    public void mergeConfigInOneFile() {
        try {
            final InputStream input = new FileInputStream(new File(basePropertiesPath));

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

            final Yaml yaml = new Yaml(constructor, new NullRepresenter(), options);
            Object object = yaml.load(input);
            logger.info("'arkcase.yaml' was loaded.");

            FileSystemResource yamlResource = new FileSystemResource(mergePropertiesPath);
            try (FileWriter fw = new FileWriter(yamlResource.getFile())) {
                yaml.dump(object, fw);
            }
        } catch (IOException e) {
            logger.error("Failed dumping the properties in 'arkcase.yaml'.", e.getMessage());
            logger.trace("Cause: ", e);
        }
    }

    private static class MyConstructor extends Constructor {
        public MyConstructor() {
            yamlConstructors.put(new Tag("!include"), new ImportConstruct());
        }
    }

    private static class NullRepresenter extends Representer {
        public NullRepresenter() {
            this.nullRepresenter = new Represent() {
                public Node representData(Object data) {
                    return representScalar(Tag.NULL, "");
                }
            };
        }
    }
}