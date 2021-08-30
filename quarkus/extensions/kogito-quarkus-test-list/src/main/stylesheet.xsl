<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:param name="input_path" select="not-set"/>
  <xsl:variable name="collection">
    <xsl:copy-of select="collection(concat($input_path,'?select=*.xml'))/testArtifacts/testArtifact" />
  </xsl:variable>
  <xsl:template match="/">
    <testArtifacts>
      <xsl:for-each select="$collection">
        <xsl:copy-of select="." />
      </xsl:for-each>
    </testArtifacts>
  </xsl:template>
</xsl:stylesheet>
