package org.juzu.impl.application.metamodel;

import org.juzu.impl.compiler.ElementHandle;
import org.juzu.impl.compiler.ProcessingContext;
import org.juzu.impl.utils.QN;

import javax.lang.model.element.Element;
import java.io.Serializable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class BufKey implements Serializable
{

   /** . */
   final QN pkg;

   /** . */
   final ElementHandle element;

   /** . */
   final String annotationFQN;

   BufKey(ProcessingContext env, Element element, String annotationFQN)
   {
      this.pkg = new QN(env.getPackageOf(element).getQualifiedName());
      this.element = ElementHandle.create(element);
      this.annotationFQN = annotationFQN;
   }

   @Override
   public int hashCode()
   {
      return element.hashCode() ^ annotationFQN.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj instanceof BufKey)
      {
         BufKey that = (BufKey)obj;
         return element.equals(that.element) && annotationFQN.equals(that.annotationFQN);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[element=" + element + ",annotation=" + annotationFQN + "]";
   }
}
