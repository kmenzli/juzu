/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.juzu.impl.controller.metamodel;

import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.metamodel.Key;
import org.juzu.impl.metamodel.MetaModel;
import org.juzu.impl.metamodel.MetaModelEvent;
import org.juzu.impl.metamodel.MetaModelObject;
import org.juzu.impl.utils.Cardinality;
import org.juzu.impl.utils.JSON;
import org.juzu.request.Phase;
import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.compiler.ElementHandle;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerMetaModel extends MetaModelObject
{

   /** A flag for handling modified event. */
   boolean modified;

   /** The application. */
   ControllersMetaModel controllers;

   /** . */
   final ElementHandle.Class handle;

   public ControllerMetaModel(ElementHandle.Class handle)
   {
      this.handle = handle;
      this.modified = false;
   }

   public JSON toJSON()
   {
      JSON json = new JSON();
      json.set("handle", handle);
      json.map("methods", getMethods());
      return json;
   }

   public ElementHandle.Class getHandle()
   {
      return handle;
   }

   public Collection<ControllerMethodMetaModel> getMethods()
   {
      return getChildren(ControllerMethodMetaModel.class);
   }
   
   public void remove(ElementHandle.Method handle)
   {
      removeChild(Key.of(handle, ControllerMethodMetaModel.class));
   }

   public ControllerMethodMetaModel addMethod(Phase phase, String name, Iterable<Map.Entry<String, String>> parameters)
   {
/*
      ArrayList<ParameterMetaModel> params = new ArrayList<ParameterMetaModel>();
      ArrayList<String> types = new ArrayList<String>();
      for (Map.Entry<String, String> entry : parameters)
      {
         params.add(new ParameterMetaModel(entry.getKey(), Cardinality.SINGLE, entry.getValue()));
         types.add(entry.getValue());
      }
      ElementHandle.Method handle = ElementHandle.Method.create(this.handle.getFQN(), name, types);
      ControllerMethodMetaModel method = new ControllerMethodMetaModel(
         handle,
         null,
         phase,
         name,
         params);
      addChild(Key.of(handle, ControllerMethodMetaModel.class), method);
      return method;
*/
      throw new UnsupportedOperationException("remove me at some point");
   }

   void addMethod(
      MetaModel context,
      ExecutableElement methodElt,
      String annotationFQN,
      Map<String, Object> annotationValues)
   {
      String id = (String)annotationValues.get("id");

      //
      for (Phase phase : Phase.values())
      {
         if (phase.annotation.getName().equals(annotationFQN))
         {
            ElementHandle.Method origin = ElementHandle.Method.create(methodElt);

            // First remove the previous method
            Key<ControllerMethodMetaModel> key = Key.of(origin, ControllerMethodMetaModel.class);
            if (getChild(key) == null)
            {
               // Validate duplicate id within the same controller
               for (ControllerMethodMetaModel existing : getChildren(ControllerMethodMetaModel.class))
               {
                  if (existing.id != null && existing.id.equals(id))
                  {
                     throw new CompilationException(methodElt, MetaModelError.CONTROLLER_METHOD_DUPLICATE_ID, id);
                  }
               }

               // Parameters
               ArrayList<ParameterMetaModel> parameters = new ArrayList<ParameterMetaModel>();
               List<? extends TypeMirror> parameterTypeMirrors = ((ExecutableType)methodElt.asType()).getParameterTypes();
               List<? extends VariableElement> parameterVariableElements = methodElt.getParameters();
               for (int i = 0;i < parameterTypeMirrors.size();i++)
               {
                  VariableElement parameterVariableElt = parameterVariableElements.get(i);
                  TypeMirror parameterTypeMirror = parameterTypeMirrors.get(i);
                  TypeMirror erasedParameterTypeMirror = context.env.erasure(parameterTypeMirror);
                  String parameterType = erasedParameterTypeMirror.toString();
                  //
                  String parameterName = parameterVariableElt.getSimpleName().toString();

                  // Determine cardinality
                  TypeMirror parameterSimpleTypeMirror;
                  Cardinality parameterCardinality;
                  switch (parameterTypeMirror.getKind())
                  {
                     case DECLARED:
                        DeclaredType dt = (DeclaredType)parameterTypeMirror;
                        TypeElement col = context.env.getTypeElement("java.util.List");
                        TypeMirror tm = context.env.erasure(col.asType());
                        TypeMirror err = context.env.erasure(dt);
                        // context.env.isSubtype(err, tm)
                        if (err.equals(tm))
                        {
                           if (dt.getTypeArguments().size() != 1)
                           {
                              throw new CompilationException(parameterVariableElt, MetaModelError.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
                           }
                           else
                           {
                              parameterCardinality = Cardinality.LIST;
                              parameterSimpleTypeMirror = dt.getTypeArguments().get(0);
                           }
                        }
                        else
                        {
                           parameterCardinality = Cardinality.SINGLE;
                           parameterSimpleTypeMirror = parameterTypeMirror;
                        }
                        break;
                     case ARRAY:
                        // Unwrap array
                        ArrayType arrayType = (ArrayType)parameterTypeMirror;
                        parameterCardinality = Cardinality.ARRAY;
                        parameterSimpleTypeMirror = arrayType.getComponentType();
                        break;
                     default:
                        throw new CompilationException(parameterVariableElt, MetaModelError.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
                  }
                  if (parameterSimpleTypeMirror.getKind() != TypeKind.DECLARED)
                  {
                     throw new CompilationException(parameterVariableElt, MetaModelError.CONTROLLER_METHOD_PARAMETER_NOT_RESOLVED);
                  }

                  //
                  TypeElement te = (TypeElement)context.env.asElement(parameterSimpleTypeMirror);
                  ElementHandle.Class a = ElementHandle.Class.create(te);

                  //
                  parameters.add(new ParameterMetaModel(parameterName, parameterCardinality, a, parameterType));
               }

               //
               ControllerMethodMetaModel method = new ControllerMethodMetaModel(
                  origin,
                  id,
                  phase,
                  methodElt.getSimpleName().toString(),
                  parameters);
               addChild(key, method);
               modified = true;
            }
            break;
         }
      }
   }

   @Override
   public boolean exist(MetaModel model)
   {
      return getChildren().size() > 0;
   }

   @Override
   protected void preDetach(MetaModelObject parent)
   {
      if (parent instanceof ControllersMetaModel)
      {
         queue(MetaModelEvent.createRemoved(this));
         controllers = null;
      }
   }

   @Override
   protected void postAttach(MetaModelObject parent)
   {
      if (parent instanceof ControllersMetaModel)
      {
         controllers = (ControllersMetaModel)parent;
         queue(MetaModelEvent.createAdded(this));
      }
   }
}
