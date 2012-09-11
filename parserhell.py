# -*- coding: utf-8 -*-

'''
createNames() returns a list of names
'''
def createNames():
	from nltk.corpus import names as nm
	list_names = []
	for fileid in nm.fileids():
		list_names += nm.words(fileid)
	list_names.sort()
	list_names = [x.lower() for x in list_names]
	#print list_names[0:26]
	return list_names

def isName(trend, list_names):
	trend_lower = trend.lower()
	for name in list_names:
		if name.lower() in trend_lower:
			return 1
	return 0

def classifyCategory(infile, filename):
	import os
	import codecs
	f_infile = codecs.open(infile, encoding='utf-8')
	splitline = []
	for line in f_infile:
		splitline = line.split(' ')
	if len(splitline) < 5:
		return '?'
	command = 'bin/mallet classify-file --input ' + infile + ' --output - --classifier tweets.classifier'
	outfile = filename
	full_command = command + ' > ' + outfile
	os.system(full_command)
	
	f = codecs.open(outfile, encoding='utf-8')
	splitline = []
	for line in f:
		splitline = line.split('\t')
	most_probable = splitline[1]
	probability = float(splitline[2])
	x = 3
	while x < len(splitline)-1:
		if float(splitline[x+1]) > probability:
			most_probable = splitline[x]
			probability = float(splitline[x+1])
		x += 2
	#print most_probable, probability
	f.close()
	return most_probable[0:2]


'''
countLinks returns an int
	count of number of url links in tweet
	matches \.../ and \..../
	ex. .ly/ or .com/
'''
def countLinks(tweet):
	import re
	count = len(re.findall('\.../', tweet))
	count += len(re.findall('\..../', tweet))
	return count

'''
parse(tweet) returns a size 4 array
	[0] = # nouns in tweet
	[1] = # adjs in tweet
	[2] = # of superlative adjs in tweet
	[3] = # of comparative adjs in tweet
'''
def parse(tweet):
	import nltk
	#nltk.download()
	POS_freq = [0,0,0,0]
	tokenized_tweet = nltk.word_tokenize(tweet)
	tagged_tweet = nltk.pos_tag(tokenized_tweet)
	for word in tagged_tweet:
		if word[1] == 'NN' or word[1] == 'NNP':
			POS_freq[0] += 1
		elif word[1] == 'JJ':
			POS_freq[1] += 1
		elif word[1] == 'JJS':
			POS_freq[2] += 1
		elif word[1] == 'JJR':
			POS_freq[3] += 1
	return POS_freq



def main():
	dirtocheck = "/Users/mfong/Desktop/research/tweets/"
	import os, os.path, codecs
	numFiles = 0
	
	'''
	The files that we will write.
		arff_file is the final WEKA stylized .arff file
		temp_news_file is the temporary file that houses the current news per trend
	'''
	arff_file = 'twitter.arff'
	temp_news_file = 'temp_news.txt'
	
	arffWriter = codecs.open(arff_file, 'w', encoding='utf-8')
	arffWriter.write('% Twitter Trend Arff\n')
	arffWriter.write('% Man Fong\n')
	arffWriter.write('% CS-4699\n')
	arffWriter.write('% class {0. 1. 2} where 0 is < 20 mins, 20 < 1 < 50 and 2 is > 50\n')
	arffWriter.write('@relation TwiterTrend\n')
	arffWriter.write('@attribute tweet-name string\n')
	arffWriter.write('@attribute retweets string\n')
	arffWriter.write('@attribute num-nouns numeric\n')
	arffWriter.write('@attribute num-adjs numeric\n')
	arffWriter.write('@attribute num-sup-adjs numeric\n')
	arffWriter.write('@attribute num-comp-adjs numeric\n')
	arffWriter.write('@attribute category {\'bu\', \'en\', \'he\', \'sc\', \'sp\', \'te\'}\n')
	#arffWriter.write('@attribute trend-length numeric\n')
	arffWriter.write('@attribute num-links numeric\n')
	arffWriter.write('@attribute is-name {0, 1}\n')
	arffWriter.write('@attribute class {0, 1, 2}\n')
	arffWriter.write('@data\n')
	

	
	print 'begin'
	for root, dirs, files in os.walk(dirtocheck):
		for f in files:
			fullpath = os.path.join(root, f)
			k0 = u''
			k1 = u''
			k2, k3, k4, k5 = 0, 0, 0, 0
			k6 = u''
			k7, k8, k9 = 0, 0, 0
			
			print numFiles, f
			if f == '.DS_Store':
 				continue

			trendFile = codecs.open('tweets/' + f, encoding='utf-8')
			lineCounter = 0

			for line in trendFile:
				if lineCounter != 3:
					line = line[0: len(line)-1]
				if lineCounter == 0:
					print line
					k0 += line
				elif lineCounter == 1:
					print 'tweets'
					k1 += line
					POS = parse(k1)
					k2, k3, k4, k5 = POS[0], POS[1], POS[2], POS[3]
					print '\t' ,k2, k3, k4, k5
					k7 = countLinks(k1)
					k1 = k1.replace("\"", " ")
				elif lineCounter == 2:
					print 'news'
					newsWriter = codecs.open(temp_news_file, 'w', encoding='utf-8')
					newsWriter.write(line)
					newsWriter.close()
					k6 += classifyCategory(temp_news_file, "TEMPREADNEWS.txt")
					print '\t', k6
				elif lineCounter == 3:
					print line[0:len(line)-1]
					trendLength = int(line[0:len(line)-1])
					if trendLength < 20:
						k9 = 0
					elif trendLength < 40:
						k9 = 1
					else:
						k9 = 2
				lineCounter += 1
			
			arffWriter.write('\"')
			arffWriter.write(k0)
			arffWriter.write('\"')
			arffWriter.write(', ')
			
			arffWriter.write('\"')
			arffWriter.write(k1)
			arffWriter.write('\"')
			arffWriter.write(', ')
			
			arffWriter.write(str(k2))
			arffWriter.write(', ')
			arffWriter.write(str(k3))
			arffWriter.write(', ')
			arffWriter.write(str(k4))
			arffWriter.write(', ')
			arffWriter.write(str(k5))
			arffWriter.write(', ')
			
			arffWriter.write('\"')
			arffWriter.write(k6)
			arffWriter.write('\"')
			arffWriter.write(', ')
			
			arffWriter.write(str(k7))
			arffWriter.write(', ')
			arffWriter.write(str(k8))
			arffWriter.write(', ')
			arffWriter.write(str(k9))
			
			arffWriter.write('\n')
			
			numFiles += 1
	print numFiles
	
	
if __name__=='__main__':
	main()
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	