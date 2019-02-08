package org.gradle.plugins.eclipseplugin

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * Builder to create jarfiles with content very easily for tests
 * @author OleyMa
 */
class JarBuilder {

  /**
   * current zipfile
   */
  private ZipOutputStream out

  private File jarFile


  /**
   * create an instance
   * @return instance
   */
  static JarBuilder create() {
    return new JarBuilder()
  }

  /**
   * private constructor
   */
  private JarBuilder() {
  }

  /**
   * create a new file
   * @param jarFile   file to create
   * @return  builder itself
   */
  public JarBuilder withFile(final File jarFile) {
    jarFile.parentFile.mkdirs()
    if (out != null)
      out.close()

    this.jarFile = jarFile
    return this
  }

  /**
   * create a new item into file with dummy content
   * @param item  item
   * @return  builder itself
   */
  public JarBuilder withContent(final String item) {
    return withContent(item, 'Dummy')
  }

  public JarBuilder withClass (final String clazz, final String prefix = null) {
    String item = clazz.replace(".", "/")  + ".class"
    File classFile = GradleUtils.getProjectPath('vsareleaseivy', "build/classes/java/test/" + item)
    if (! classFile.exists())
      throw new IllegalStateException("Classfile " + classFile.absolutePath + " does not exist")
    if (prefix != null)
      item = prefix + item
    withContent(item, classFile.text)
  }



  /**
   * create a new item into file with dummy content
   * @param item  item
   * @param content item content
   * @return  builder itself
   */
  public JarBuilder withContent(final String item, final String content) {

    // Add ZIP entry to output stream.
    if (out == null)
      out = new ZipOutputStream(new FileOutputStream(jarFile));

    out.putNextEntry(new ZipEntry(item))
    out.write(content.bytes)
    out.closeEntry()

    // Complete the ZIP file

    return this
  }

  /**
   * closes the stream
   * @return
   */
  public JarBuilder finish () {
    out.close()
    out = null
    return this
  }
}