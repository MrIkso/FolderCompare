<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"/>

<xsl:template match="/DOCUMENT">
<html>
	<head>
		<title>
			Folder Comparison Report
		</title>
		<style type="text/css">
			body { background: #fff;  color: #000; margin: 0px 14px 14px 14px; }
			h1, h2, h3, h4, h5 {
				padding: 0em;
				margin: 1.5em 0em .25em 0em;
				text-align: left;
				font-weight: normal;
				font-family: Arial, Helvetica, sans-serif;
				font-size: 70%;
				max-width: 55em;
			}
			body, h4, h5, p, ol, ul, li, td, th { font-family: Verdana, Arial, Helvetica, sans-serif; text-align: left; }
			
			h4, h5, p, ol, ul, li { font-size: 70%; }
			input {
			  font-family: Arial, Helvetica, sans-serif;
			  font-size: 120%;
			}
			
			h1 { margin: 14px 0em 1.2em 0em; font-size: 112%; line-height: 130%;}
			h2 { font-size: 125%; line-height: 84%;}
			h3 { margin-top: 1.4em; font-size: 81%; }
			h4 { margin-top: 1em; font-size: 70%; }
			h5 { margin-top: 1em; font-size: 100%; font-style: italic;}
			h1 strong, h2 strong, h3 strong, h4 strong { font-weight: bold; }
			
			p { margin: 6px 0em 6px 0em; max-width: 55em;}
			p.code, code {}
			p { font-size: 70%; line-height: 125%; }
			table { font-size: 70%;}
			
			p.copyright, .small { font-size: 60%; line-height: 120%; }
			p.copyright { margin: 3px 0px 0px 0px; padding: 0px; text-align: left; }
			acronym { font-variant: small-caps; }
			div.pad { padding: 3px; }
			span.lineEndings { color: red; font-size: 70%; }
			
			table.mgRowGroup, table.mgRowGroupFolder { border: 1px solid #a0a0a0; margin: 10px 0px 10px 0px; width: 100%; table-layout: fixed; border-collapse: collapse; }
			table.mgRowGroup td { vertical-align: top; padding: 0; }
			table.mgRowGroupFolder td { vertical-align: top; padding: 1px 0 1px 0; }
			div.IP { width: 2px; height: 1em; float: left; }
			
			img.mgFldrIcon {float: left; text-align: left; margin: -1px 2px 0px 0px;  height: 32px; width: 32px; border: none; vertical-align: top; background-color: transparent;}
			div.mgFldrIcon {float: left; width: auto;}
			span.left {float: left;width: auto;}
			span.right {float: right;width: auto;}
			
			table.bordered { border-collapse: collapse; border: 1px solid #a0a0a0; margin: 10px 0px 10px 0px;}
			table.bordered th, table.bordered td { vertical-align: top; padding: 3px; border: 1px solid #a0a0a0;}
			table.bordered th { background-color: #e5e5e5; }
			table.bordered td.group { background-color: #f5f5f5; }
			span.disabledNavigation { color: #a0a0a0; }
			div.navigation { margin-top: 14px; font-size: 70%; }
							
			                span.U1, span.U1 a {float: left; width: auto;color: #000000; background-color: #ffffff; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.U1 a {text-decoration: underline;}
			td.U1 {color: #000000; background-color: #ffffff; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.R1, span.R1 a {float: left; width: auto;color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.R1 a {text-decoration: underline;}
			td.R1 {color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.I1, span.I1 a {float: left; width: auto;color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.I1 a {text-decoration: underline;}
			td.I1 {color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.C1, span.C1 a {float: left; width: auto;color: #000000; background-color: #a6caf0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.C1 a {text-decoration: underline;}
			td.C1 {color: #000000; background-color: #a6caf0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			td.nbgC1 {color: #000000; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; background-color: #ffffff; }
			span.U2, span.U2 a {float: left; width: auto;color: #000000; background-color: #ffffff; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.U2 a {text-decoration: underline;}
			td.U2 {color: #000000; background-color: #ffffff; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.R2, span.R2 a {float: left; width: auto;color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.R2 a {text-decoration: underline;}
			td.R2 {color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.I2, span.I2 a {float: left; width: auto;color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.I2 a {text-decoration: underline;}
			td.I2 {color: #000000; background-color: #c0dcc0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.C2, span.C2 a {float: left; width: auto;color: #000000; background-color: #a6caf0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			span.C2 a {text-decoration: underline;}
			td.C2 {color: #000000; background-color: #a6caf0; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; }
			td.nbgC2 {color: #000000; font-family: "Tahoma"; font-weight: normal; font-style: normal; font-size: 8pt; text-decoration: none; background-color: #ffffff; }
			
			span.selected { background-color: highlight; color: highlighttext; }
			span.selected a {background-color: highlight; color: highlighttext; text-decoration: underline; }
		</style>	
	</head>

	<body>
		<h1><strong>Folder Comparison Report</strong></h1>
		<xsl:apply-templates select="Timestamp"/>
		<xsl:apply-templates select="Folders"/>
		<xsl:apply-templates select="Statistics"/>
		<xsl:apply-templates select="CompareItems"/>
	</body>
</html>
</xsl:template>

<xsl:template match="Timestamp">
<p>Produced by <strong>Folder Compare</strong> on <strong><xsl:value-of select="."/></strong>.</p>
</xsl:template>

<xsl:template match="Folders">
	<h3><strong>1. Folders compared</strong></h3>
	<table class="bordered">
	<tr>
	<th>#</th>
	<th>Location</th>
	<th>Folder</th>
	</tr>
	<xsl:apply-templates select="Folder"/>
	</table>
</xsl:template>

<xsl:template match="Folder">
	<tr>
	<td><xsl:value-of select="position()"/></td>
	<td><xsl:value-of select="@Location"/></td>
	<td><xsl:value-of select="@FolderName"/></td>
	</tr>
</xsl:template>

<xsl:template match="Statistics">
	<h3><strong>2. Summary for all files</strong></h3>
	<table class="bordered">
	<tr>
	<th>Between Folder 1 and 2</th>
	</tr>
	<tr><td><xsl:value-of select="Changed"/> changed files</td></tr>
	<tr><td><xsl:value-of select="Unchanged"/> unchanged files</td></tr>
	<tr><td><xsl:value-of select="Inserted"/> inserted files</td></tr>
	<tr><td><xsl:value-of select="Removed"/> removed files</td></tr>
	<tr><td><xsl:value-of select="Total"/> totally compared</td></tr>
	</table>
</xsl:template>

<xsl:template match="CompareItems">
	<h3><strong>3. Comparison detail</strong></h3>
	<table class="mgRowGroupFolder" style="padding: 1px 0 1px 0;">
	<col width="width: 50%;" />
	<col style="width: 2.5em;" />
	<col style="width: .8em;" />
	<col width="width: 50%;" />
	<xsl:apply-templates select="CompareRow"/>
	</table>
</xsl:template>

<xsl:template match="CompareRow">
	<tr>
	<xsl:apply-templates select="ItemLeft"/>
	<td class="U1" style="text-align: right; text-decoration: none;"> </td>
	<td> </td>
	<xsl:apply-templates select="ItemRight"/>
	</tr>
</xsl:template>

<xsl:template match="ItemLeft">
	<xsl:choose>
		<xsl:when test="@empty = 'true'">
			<xsl:text disable-output-escaping="yes">
				<![CDATA[<td style="background-color: #f5f5f5;">]]>
			</xsl:text>
		</xsl:when>
		<xsl:when test="@empty = 'false'">
			<xsl:choose>
				<xsl:when test="@unique = 'false'">
					<xsl:choose>
						<xsl:when test="@equal = 'true'">
							<xsl:text disable-output-escaping="yes">
								<![CDATA[<td class="C1">]]>
							</xsl:text>
						</xsl:when>
						<xsl:when test="@equal = 'false'">
							<xsl:text disable-output-escaping="yes">
								<![CDATA[<td class="U1">]]>
							</xsl:text>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="@unique = 'true'">
					<xsl:text disable-output-escaping="yes">
						<![CDATA[<td class="R1">]]>
					</xsl:text>
				</xsl:when>
			</xsl:choose>
			<div class="mgFldrIcon" style="padding-left: 0px;">
			<xsl:choose>
				<xsl:when test="@dir = 'true'">
					<img alt="" class="mgFldrIcon" src="FolderComparisonReport/folder.png" />
				</xsl:when>
				<xsl:when test="@dir = 'false'">
					<img alt="" class="mgFldrIcon" src="FolderComparisonReport/file.png" />
				</xsl:when>
			</xsl:choose>
			</div><span class="left">
			<span><xsl:value-of select="."/></span>
			</span>
		</xsl:when>
	</xsl:choose>
	<xsl:text disable-output-escaping="yes">
		<![CDATA[</td>]]>
	</xsl:text>
</xsl:template>

<xsl:template match="ItemRight">
	<xsl:choose>
		<xsl:when test="@empty = 'true'">
			<xsl:text disable-output-escaping="yes">
				<![CDATA[<td style="background-color: #f5f5f5;">]]>
			</xsl:text>
		</xsl:when>
		<xsl:when test="@empty = 'false'">
			<xsl:choose>
				<xsl:when test="@unique = 'false'">
					<xsl:choose>
						<xsl:when test="@equal = 'true'">
							<xsl:text disable-output-escaping="yes">
								<![CDATA[<td class="C2">]]>
							</xsl:text>
						</xsl:when>
						<xsl:when test="@equal = 'false'">
							<xsl:text disable-output-escaping="yes">
								<![CDATA[<td class="U2">]]>
							</xsl:text>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="@unique = 'true'">
					<xsl:text disable-output-escaping="yes">
						<![CDATA[<td class="R2">]]>
					</xsl:text>
				</xsl:when>
			</xsl:choose>
			<div class="mgFldrIcon" style="padding-left: 0px;">
			<xsl:choose>
				<xsl:when test="@dir = 'true'">
					<img alt="" class="mgFldrIcon" src="FolderComparisonReport/folder.png" />
				</xsl:when>
				<xsl:when test="@dir = 'false'">
					<img alt="" class="mgFldrIcon" src="FolderComparisonReport/file.png" />
				</xsl:when>
			</xsl:choose>
			</div><span class="left">
			<span><xsl:value-of select="."/></span>
			</span>
		</xsl:when>
	</xsl:choose>
	<xsl:text disable-output-escaping="yes">
		<![CDATA[</td>]]>
	</xsl:text>
</xsl:template>

</xsl:stylesheet>