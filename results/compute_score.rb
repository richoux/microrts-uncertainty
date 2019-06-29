#!/usr/bin/ruby

def usage
  puts  "Usage: " + $0 + " FILE"
end

# We must have at least a file name
if ARGV.length == 0
  usage
  exit
end

# file = File.open(ARGV[0])

wins = (IO.readlines(ARGV[0])[-17]).split(', ')[1].to_i
ties = (IO.readlines(ARGV[0])[-14]).split(', ')[1].to_i
losses = (IO.readlines(ARGV[0])[-11]).split(', ')[1].to_i

score = wins + (ties.to_f / 2)

puts "Wins: #{wins}"
puts "Ties: #{ties}"
puts "Losses: #{losses}"
puts "Score: #{score}"

exit
