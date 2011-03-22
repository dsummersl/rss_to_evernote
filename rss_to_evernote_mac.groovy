import javax.script.*

def xml = new XmlSlurper().parse(new File("starred.html"))
def notebook_name = "googlereader"
ScriptEngineManager mgr = new ScriptEngineManager()
ScriptEngine engine = mgr.getEngineByName("AppleScript")
xml.entry.each {
  def title = it.title.text()
  def href = it.link.'@href'
  def content = it.content.text()
  def dateStr = it.updated.text()
  def tags = ""
  println "${dateStr}: ${title}"
  def addNote = makeAppleScript(title,href,content,dateStr,tags,notebook_name)
  //println addNote
   
  try {
    engine.eval(addNote)
  } catch (ScriptException se) {
    println "Error processing '${title}' (${href}). Trying w/o HTML content..." 
    try {
      engine.eval(makeAppleScript(title,href,"Couldn't import the content so just go to: ${href}",dateStr,tags,notebook_name))
    } catch (ScriptException se2) {
      println "Error processing '${title}' (${href}). Skipping..."
    }
  }
}

def makeAppleScript(title,href,content,dateStr,tags,notebook_name) {
  def date = new Date().parse("yyyy-MM-dd'T'H:m:s'Z'",dateStr)
  return """
    on epoch2date(epoch)
      set datestring to epoch
      set dateobj to current date
      set _rest to datestring
      set _sep to offset of "." in _rest
      set yearstr to text 1 through (_sep - 1) of _rest
      set _rest to text (_sep + 1) through (length of _rest) of _rest
      set _sep to offset of "." in _rest
      set monthstr to text 1 through (_sep - 1) of _rest
      set _rest to text (_sep + 1) through (length of _rest) of _rest
      set _sep to offset of "." in _rest
      set daystr to text 1 through (_sep - 1) of _rest
      set _rest to text (_sep + 1) through (length of _rest) of _rest
      set _sep to offset of "." in _rest
      set hourstr to text 1 through (_sep - 1) of _rest
      set _rest to text (_sep + 1) through (length of _rest) of _rest
      set _sep to offset of "." in _rest
      set minstr to text 1 through (_sep - 1) of _rest
      set _rest to text (_sep + 1) through (length of _rest) of _rest
      set _sep to offset of "." in _rest
      set secstr to text 1 through (_sep - 1) of _rest
      set year of dateobj to yearstr as integer
      set monthconsts to {January, February, March, April, May, June, July, August, September, October, November, December}
      set month of dateobj to (item (monthstr as integer) of monthconsts)
      set day of dateobj to daystr as integer
      set time of dateobj to ((hourstr as integer) * 3600) + ((minstr as integer) * 60) + (secstr as integer)
      return dateobj
    end epoch2date

    set cleantitle to "${title.replaceAll('"','\\\\"')}"
    set curdesc to "${content.replaceAll('\n','').replaceAll('"','\\\\"')}"
    set curtags to "${tags}"
    set curdateobj to epoch2date("${String.format('%1\$tY.%1\$tm.%1\$td.%1\$tH.%1\$tM.%1\$tS',date)}")
    set curdateobj to epoch2date("${String.format('%1\$tY.%1\$tm.%1\$td.%1\$tH.%1\$tM.%1\$tS',date)}")
    set notebook_name to "${notebook_name}"
    set cururl to "${href}"

    tell application "Evernote"
      if (not (notebook named notebook_name exists)) then
        -- NOTE also check out the "create notebook" command
        make notebook with properties {name:notebook_name}
      end if

			set savedDelimiters to AppleScript's text item delimiters
			set AppleScript's text item delimiters to {","}
      set newnote to create note title cleantitle with html curdesc tags curtags created curdateobj notebook notebook_name
      set AppleScript's text item delimiters to savedDelimiters
      set source URL of newnote to cururl
      set modification date of newnote to curdateobj
    end tell
  """
}
