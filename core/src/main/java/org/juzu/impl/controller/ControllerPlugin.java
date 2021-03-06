package org.juzu.impl.controller;

import org.juzu.impl.controller.descriptor.ControllerDescriptor;
import org.juzu.impl.controller.metamodel.ControllerMetaModelPlugin;
import org.juzu.impl.metadata.Descriptor;
import org.juzu.impl.metamodel.MetaModelPlugin;
import org.juzu.impl.plugin.Plugin;
import org.juzu.impl.utils.JSON;

import javax.annotation.processing.SupportedAnnotationTypes;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@SupportedAnnotationTypes({"org.juzu.View","org.juzu.Action","org.juzu.Resource"})
public class ControllerPlugin extends Plugin
{

   public ControllerPlugin()
   {
      super("controller");
   }

   @Override
   public MetaModelPlugin newMetaModelPlugin()
   {
      return new ControllerMetaModelPlugin();
   }

   @Override
   public Descriptor loadDescriptor(ClassLoader loader, JSON config) throws Exception
   {
      return new ControllerDescriptor(loader, config);
   }
}
