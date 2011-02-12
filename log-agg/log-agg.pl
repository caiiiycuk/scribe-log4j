#!/usr/bin/perl -w

use strict;
use utf8;
use File::ReadBackwards;

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
# 
#

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
    my ($template, $error_full, $count) = @_;

    if ($error_full =~ /$accept_message/) {
#	$error_full =~ s/\n/<br\/>/ig;

	if ($error_full =~ $new_message) {
	    my $time = "$1 ($count)";
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

#
# Read log files into messages hash
#
my %messages = ();

foreach my $log (@log_files) {
    my $file = File::ReadBackwards->new("$log") || die "can't read file '$log': $!\n";

    my $message = "";
    while (defined($_ = $file->readline)) {
	$message = $_ . $message;

	if (/$new_message/i) {
	    $messages{$message} = 0 if (!exists $messages{$message});
	    $messages{$message}++;

	    last if (scalar(values(%messages)) > $max_count);
	    
	    $message = "";
	}
    }

    $file->close();
}

#
# Open resources
#
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

#
# Messages template
#
my $template = $_;
while (<TEMPLATE>) {
    $template = $template .  $_;
    last if (/<!-- MESSAGE END -->/);
}

#
# Write messages
#
foreach my $message (keys %messages) {
    my $prepared = prepareMessage($template, $message, $messages{$message});
    print INDEX $prepared if ($prepared);
}

#
# Write footer
#
while (<TEMPLATE>) {
    print INDEX;
}

#
# Close resources
#
close(INDEX);
close(TEMPLATE);
