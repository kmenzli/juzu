package org.juzu.test;

import junit.framework.Assert;
import org.juzu.impl.compiler.*;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.fs.Change;
import org.juzu.impl.fs.FileSystemScanner;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.fs.SimpleFileSystem;
import org.juzu.impl.utils.Tools;

import javax.annotation.processing.Processor;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class CompileStrategy<I, O>
{

   /** . */
   final SimpleFileSystem<?> classPath;

   /** . */
   final ReadWriteFileSystem<I> sourcePath;

   /** . */
   final ReadWriteFileSystem<O> sourceOutput;

   /** . */
   final ReadWriteFileSystem<O> classOutput;

   /** . */
   Provider<? extends Processor> processorFactory;

   public CompileStrategy(
      SimpleFileSystem<?> classPath,
      ReadWriteFileSystem<I> sourcePath,
      ReadWriteFileSystem<O> sourceOutput,
      ReadWriteFileSystem<O> classOutput,
      Provider<? extends Processor> processorFactory)
   {
      this.classPath = classPath;
      this.sourcePath = sourcePath;
      this.sourceOutput = sourceOutput;
      this.classOutput = classOutput;
      this.processorFactory = processorFactory;
   }

   final Compiler.Builder compiler()
   {
      Compiler.Builder builder = Compiler.builder();
      builder.addClassPath(classPath);
      builder.sourcePath(sourcePath);
      builder.sourceOutput(sourceOutput);
      builder.classOutput(classOutput);
      return builder;
   }

   Compiler compiler;

   abstract List<CompilationError> compile() throws IOException;

   abstract void addClassPath(ReadFileSystem<?> classPath);

   /** . */
   private static final Pattern javaFilePattern = Pattern.compile("(.+)\\.java");

   public static class Incremental<I, O> extends CompileStrategy<I, O>
   {

      /** . */
      final LinkedList<ReadFileSystem<?>> classPath;

      /** . */
      final FileSystemScanner<I> scanner;

      public Incremental(SimpleFileSystem<?> classPath, ReadWriteFileSystem<I> sourcePath, ReadWriteFileSystem<O> sourceOutput, ReadWriteFileSystem<O> classOutput, Provider<? extends Processor> processorFactory)
      {
         super(classPath, sourcePath, sourceOutput, classOutput, processorFactory);

         //
         this.classPath = new LinkedList<ReadFileSystem<?>>();
         this.scanner = FileSystemScanner.createHashing(sourcePath);
      }

      List<CompilationError> compile() throws IOException
      {
         Compiler.Builder builder = compiler();

         // Force compilation
         builder.force(true);

         //
         List<String> toCompile = new ArrayList<String>();
         List<String> toDelete = new ArrayList<String>();

         //
         for (Map.Entry<String, Change> change : scanner.scan().entrySet())
         {
            String path = change.getKey();
            if (path.endsWith(".java"))
            {
               switch (change.getValue())
               {
                  case REMOVE:
                     toDelete.add(path);
                     break;
                  case ADD:
                     toCompile.add(path);
                     break;
                  case UPDATE:
                     toCompile.add(path);
                     toDelete.add(path);
                     break;
               }
            }
         }

         //
         for (String s : toDelete)
         {
            Matcher matcher = javaFilePattern.matcher(s);
            Assert.assertTrue(matcher.matches());
            String path = matcher.group(1) + ".class";
            String[] names = Tools.split(path, '/');
            O clazz = classOutput.getPath(names);
            if (clazz != null)
            {
               classOutput.removePath(clazz);
            }
         }

         //
         for (ReadFileSystem<?> cp : classPath)
         {
            builder.addClassPath(cp);
         }

         //
         System.out.println("Compiling " + toCompile);
         compiler = builder.build(processorFactory != null ? processorFactory.get() : null);
         return compiler.compile(toCompile.toArray(new String[toCompile.size()]));
      }

      @Override
      void addClassPath(ReadFileSystem<?> classPath)
      {
         this.classPath.add(classPath);
      }
   }

   public static class Global<I, O> extends CompileStrategy<I, O>
   {
      public Global(SimpleFileSystem<?> classPath, ReadWriteFileSystem<I> sourcePath, ReadWriteFileSystem<O> sourceOutput, ReadWriteFileSystem<O> classOutput, Provider<? extends Processor> processorFactory)
      {
         super(classPath, sourcePath, sourceOutput, classOutput, processorFactory);
      }

      List<CompilationError> compile() throws IOException
      {
         Compiler.Builder builder = compiler();
         compiler = builder.build(processorFactory != null ? processorFactory.get() : null);
         return compiler.compile();
      }

      @Override
      void addClassPath(ReadFileSystem<?> classPath)
      {
      }
   }
}
