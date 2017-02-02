/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2016, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.templateclassgenerator.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.monticore.ast.ASTNode;
import de.monticore.generating.ExtendedGeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.templateclassgenerator.EmptyNode;
import de.se_rwth.commons.Names;
import freemarker.cache.FileTemplateLoader;
import freemarker.core.FMHelper;
import freemarker.core.Parameter;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * This class generates a template class for each template.
 * 
 * @author Jerome Pfeiffer
 */
public class TemplateClassGenerator {
  
  /**
   * Generates the template fqnTemplateName from the modelPath to the
   * targetFilePath with the targetName
   * 
   * @param targetName
   * @param modelPath
   * @param fqnTemplateName
   * @param targetFilepath
   */
  public static void generateClassForTemplate(String targetName, Path modelPath,
      String fqnTemplateName,
      File targetFilepath) {
    List<Parameter> params = new ArrayList<>();
    Optional<String> result = Optional.empty();
    Configuration config = new Configuration();
    Template t = null;
    boolean hasSignature = false;
    try {
      config.setTemplateLoader(new FileTemplateLoader(modelPath.toFile()));
      t = config.getTemplate(fqnTemplateName);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    Map<String, List<List<String>>> methodCalls = FMHelper.getMethodCalls(t);
    if (methodCalls.containsKey(TemplateClassGeneratorConstants.PARAM_METHOD)) {
      // we just recognize the first entry as there
      // must not be multiple params definitions
      hasSignature = true;
      params = FMHelper
          .getParams(methodCalls.get(TemplateClassGeneratorConstants.PARAM_METHOD).get(0));
    }
    
    if (methodCalls.containsKey(TemplateClassGeneratorConstants.RESULT_METHOD)) {
      // A template can only have one result type.
      String dirtyResult = methodCalls.get(TemplateClassGeneratorConstants.RESULT_METHOD).get(0)
          .get(0);
      String cleanResult = dirtyResult.replace("\"", "");
      result = Optional.of(cleanResult);
    }
    
    doGenerateTemplateClass(targetFilepath, fqnTemplateName, targetName, params, result, hasSignature);
  }
  
  /**
   * Does the generation with the parameters of the signature method
   * tc.params(...) and tc.signature(...).
   * 
   * @param targetFilepath
   * @param fqnTemplateName
   * @param targetName
   * @param params
   * @param result
   */
  private static void doGenerateTemplateClass(File targetFilepath, String fqnTemplateName, String targetName,
      List<Parameter> params, Optional<String> result, boolean hasSignature) {
    final GeneratorSetup setup = new GeneratorSetup(targetFilepath);
    GlobalExtensionManagement glex = new GlobalExtensionManagement();
    glex.setGlobalValue("TemplateClassPackage",
        TemplateClassGeneratorConstants.TEMPLATE_CLASSES_PACKAGE);
    glex.setGlobalValue("TemplateClassSetupPackage", TemplateClassGeneratorConstants.TEMPLATE_CLASSES_SETUP_PACKAGE);
    setup.setGlex(glex);
    TemplateClassHelper helper = new TemplateClassHelper();
    final ExtendedGeneratorEngine generator = new ExtendedGeneratorEngine(setup);
    ASTNode node = new EmptyNode();
    String packageNameWithSeperators = TemplateClassGeneratorConstants.TEMPLATE_CLASSES_PACKAGE
        + File.separator
        + Names.getPathFromFilename(fqnTemplateName);
    String packageNameWithDots = Names.getPackageFromPath(packageNameWithSeperators);
    generator.generate("typesafety.TemplateClass",
        Paths.get(packageNameWithSeperators, targetName + ".java"), node,
        packageNameWithDots, fqnTemplateName, targetName, params, result, hasSignature, helper);
  }
  
  /**
   * Generates a TemplateStorage class, which contains all generated template
   * classes. Further it generates a generator config class to configure the
   * used generator engine in template classes and a setup template to configure
   * the static use of template classes within a template.
   * 
   * @param foundTemplates
   * @param targetFilepath
   * @param modelPath
   * @param foundTemplates
   */
  public static void generateTemplateSetup(File targetFilepath, File modelPath,
      List<String> foundTemplates) {
    String packageName = TemplateClassGeneratorConstants.TEMPLATE_CLASSES_PACKAGE+"."+TemplateClassGeneratorConstants.TEMPLATE_CLASSES_SETUP_PACKAGE;
    final GeneratorSetup setup = new GeneratorSetup(targetFilepath);
    setup.setTracing(false);
    GlobalExtensionManagement glex = new GlobalExtensionManagement();
    glex.setGlobalValue("TemplatePostfix",
        TemplateClassGeneratorConstants.TEMPLATE_CLASSES_POSTFIX);
    glex.setGlobalValue("TemplateClassPackage",
        TemplateClassGeneratorConstants.TEMPLATE_CLASSES_PACKAGE);
    glex.setGlobalValue("TemplatesAlias", TemplateClassGeneratorConstants.TEMPLATES_ALIAS);
    setup.setGlex(glex);
    final ExtendedGeneratorEngine generator = new ExtendedGeneratorEngine(setup);
    
    String filePath = Names.getPathFromPackage(packageName) + File.separator;
    String mp = modelPath.getPath();
    List<File> nodes = TemplateClassHelper.walkTree(modelPath);
    List<String> templates = foundTemplates;
    generator.generate("typesafety.setup.TemplateAccessor", Paths.get(filePath + "TemplateAccessor.java"),
        new EmptyNode(),
        packageName, templates, mp, new TemplateClassHelper());
    generator.generate("typesafety.setup.Setup", Paths.get(filePath + "Setup.ftl"),
        new EmptyNode(),
        nodes, mp,
        new TemplateClassHelper(), new ArrayList<File>());
    generator.generate("typesafety.setup.GeneratorConfig",
        Paths.get(filePath + "GeneratorConfig.java"),
        new EmptyNode(), packageName,getRelativeTargetPath(setup.getOutputDirectory().getAbsolutePath()) );
  }
  
  private static String getRelativeTargetPath(String absolutePath) {
    String homeDir = Paths.get("").toFile().getAbsolutePath();
    if(absolutePath.contains(homeDir)){
      String relPath = absolutePath.replace(homeDir, "");
      if(relPath.startsWith(File.separator)){
        relPath = relPath.substring(1);
      }
      return relPath.replace(File.separator, "/");
    }
    return absolutePath.replace(File.separator, "/");
    
    
  }
  
}
