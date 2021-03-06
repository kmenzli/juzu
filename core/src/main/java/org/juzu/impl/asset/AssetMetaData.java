package org.juzu.impl.asset;

import org.juzu.asset.AssetLocation;
import org.juzu.impl.utils.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes an asset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AssetMetaData
{

   /** The asset id. */
   final String id;

   /** The asset location. */
   final AssetLocation location;

   /** The asset value. */
   final String value;

   /** The asset dependencies. */
   final Set<String> dependencies;

   public AssetMetaData(String id, AssetLocation location, String value, String... dependencies)
   {
      this.id = id;
      this.value = value;
      this.location = location;
      this.dependencies = Collections.unmodifiableSet(Tools.set(dependencies));
   }

   public String getId()
   {
      return id;
   }

   public AssetLocation getLocation()
   {
      return location;
   }

   public String getValue()
   {
      return value;
   }

   public Set<String> getDependencies()
   {
      return dependencies;
   }

   @Override
   public String toString()
   {
      return "AssetDescriptor[id=" + id + ",location=" + location + ",value=" + value + ",dependencies=" + Arrays.asList(dependencies) + "]";
   }
}
