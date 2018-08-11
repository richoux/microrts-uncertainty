#!/usr/bin/ruby

def usage
  puts  "Usage: " + $0 + " FILE"
end

# We must have at least a file name
if ARGV.length == 0
  usage
  exit
end

file = File.open(ARGV[0])
  
# For each line in file
file.each do |line|
  words = line.split(': ')
  if not words[0] == nil and not words[1] == nil
    numbers = words[1].split(', ')
    numbers.each do |num|
      if not num == nil and not num.include? "-"
        percent = (num.to_f / 10).round(1)
        print "#{percent} "
      end
    end
    puts "\n"      
  else
    puts "**************\n"
  end
end

exit
