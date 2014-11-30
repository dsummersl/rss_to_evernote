import javax.script.*

def cli = new CliBuilder(
  usage: "groovy import_file_to_evernote.groovy [options] {file}",
  footer: "\nImports file into Evernote, tagged as 'automated'"
)
cli.t(args:1,argName:'name',"Add tag to imported file.")
def options = cli.parse(args)

if (options.arguments().size() != 1) {
  cli.usage()
  System.exit(1)
}
ScriptEngineManager mgr = new ScriptEngineManager()
ScriptEngine engine = mgr.getEngineByName("AppleScript")

def addNote = ""
try {
  def file = new File(options.arguments()[0])
  if (!file.exists()) {
    println "File '${file}' doesn't exist!"
    System.exit(1)
  }
  if (!file.isFile()) {
    println "'${file}' is not a file!"
    System.exit(1)
  }
  addNote = makeAppleScript(file.name, "automated,${options.t}".split(","), file.getAbsoluteFile())
  engine.eval(addNote)
} catch (ScriptException se) {
  println se
  println ""
  println addNote
}

def makeAppleScript(title,tags,filename) {
  def tagString = tags.collect { "\"${it}\""}.join(",")
  return """
    set cleantitle to "${title.replaceAll('"','\\\\"')}"
    set curtags to { ${tagString} }
    set curfile to "${filename}"

    tell application "Evernote"
			set savedDelimiters to AppleScript's text item delimiters
			set AppleScript's text item delimiters to {","}
      set newnote to create note from file curfile tags curtags title cleantitle
      set AppleScript's text item delimiters to savedDelimiters
    end tell
  """
}
