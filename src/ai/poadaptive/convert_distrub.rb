#!/usr/bin/ruby
# coding: utf-8

# To convert a distribution.xml file into a simple text file

def usage
  puts  "Usage: " + $0 + " FILE"
end

# We must have at least a file name
if ARGV.length == 0
  usage
  exit
end

name = ARGV[0].split(".xml")[0]
new_file = name + ".txt"

# Read the unique(!) line in the xml file
line = IO.readlines(ARGV[0])

times = line.to_s.split("time=")
timehash = Hash.new

times.each do |time|
  key_time = time[/"([^"]*)/,1].split('\\')[0].to_i
  worker = time[/<worker>([^<\/worker>]*)/,1]
  ranged = time[/<ranged>([^<\/ranged>]*)/,1]
  light = time[/<light>([^<\/light>]*)/,1]
  heavy = time[/<heavy>([^<\/heavy>]*)/,1]

  timehash[key_time] = "worker:" + worker.to_s + ":ranged:" + ranged.to_s + ":light:" + light.to_s + ":heavy:" + heavy.to_s
end

#timehash.sort.to_h

begin
  file = File.open( new_file, "w" )
  timehash.sort.map do |k, v|
    time_line = "Time:" + k.to_s + "\n"
    file.write( time_line )
    values = v.split(':')
    worker_line = "worker:" + values[1].to_s + "\n"
    ranged_line = "ranged:" + values[3].to_s + "\n"
    light_line = "light:" + values[5].to_s + "\n"
    heavy_line = "heavy:" + values[7].to_s + "\n"
    file.write( worker_line ) 
    file.write( ranged_line ) 
    file.write( light_line ) 
    file.write( heavy_line ) 
  end
rescue IOError => e
#some error occur, dir not writable etc.
ensure
  file.close unless file.nil?
end


exit
