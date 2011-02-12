#!/usr/bin/perl -w

use strict;
use utf8;

# =============
# Configuration
# =============

#
# List of log-files for look
#
my @log_files = ('/opt/production/portal/tomcat1_old/logs/catalina.out');

#
# RegExp for new message in log
# For example: 2011-11-11 14:45:33, 444 ERROR Service ..., 
# mathcing with '^\d{4,4}-\d{2,2}-\d{2,2}\s{1,1}\d{2,2}:\d{2,2}:\d{2,2},\d{1,3}\s{1,1}'
#
my $new_message = '^(\d{4,4}-\d{2,2}-\d{2,2}\s{1,1}\d{2,2}:\d{2,2}:\d{2,2},\d{1,3})\s{1,1}';

#
# Types of message to accept
#
my $accept_message = 'ERROR';

#
# Count of messages to display
#
my $max_count = 50;

#
# Target file (index.html)
#
my $target = '/opt/WWW/dl.4geo.ru/tmp/index.html';

# ================
# Function`s scope
# ================

sub prepareMessage {
    my ($template, $error_full) = @_;

    if ($error_full =~ /$accept_message/) {
#	$error_full =~ s/\n/<br\/>/ig;

	if ($error_full =~ $new_message) {
	    my $time = $1;
	    $template =~ s/\$error-time/$time/ig;
	}

	$template =~ s/\$error-full/$error_full/ig;
	
	return $template;
    }

    return "";
}

# ============
# Main program
# ============

open(TEMPLATE, "template.html") || die $!;
binmode(TEMPLATE, ":utf8");

open(INDEX, ">$target") || die $!;
binmode(INDEX, ":utf8");

#
# Write header
#

while (<TEMPLATE>) {
    last if (/<!-- MESSAGE START -->/);
    print INDEX;
}

my $template = $_;
my $count = 0;

#
# Messages template
#
while (<TEMPLATE>) {
    $template = $template .  $_;
    last if (/<!-- MESSAGE END -->/);
}

foreach my $log (@log_files) {
    open(LOG, $log) || die $!;
    binmode(LOG, ":utf8");

    my $message = "";
    while (<LOG>) {
	if (/$new_message/i) {
	    my $prepared = prepareMessage($template, $message);
	    
	    if ($prepared) {
		print INDEX $prepared;
		$count++;
		last if ($count == $max_count);
	    }

	    $message = "";
	}

	$message = $message . $_;
    }

    print INDEX prepareMessage($template, $message);
    close(LOG);
}

#
# Write footer
#

while (<TEMPLATE>) {
    print INDEX;
}

close(INDEX);
close(TEMPLATE);
