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

total = 0
win = 0
tie = 0
lost = 0
  
# For each line in file
file.each do |line|
  words = line.split(': ')
  if not words[0] == nil
    if words[0].include? "Winner"
      total += 1
      finewords = words[1].split(' ')
      winner = finewords[0].to_i
      if winner == 1
        win += 1
      elsif winner == 0
        tie += 1
      else
        lost += 1
      end
    end
  end
end

percent_win = (100 * win / total).round(1)
percent_tie = (100 * tie / total).round(1)
percent_lost = (100 * lost / total).round(1)


puts "#{ARGV[0]} over #{total} matches:\n"
puts "win=#{win} (#{percent_win})\%"
puts "tie=#{tie} (#{percent_tie})\%"
puts "lost=#{lost} (#{percent_lost})\%"

exit
