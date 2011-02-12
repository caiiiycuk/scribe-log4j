#!/usr/bin/perl -w

use strict;
use utf8;

# =============
# Configuration
# =============

my @log_files = ('/home/caiiiycuk/exmp.log');
my $new_message = '^\d{1,2}\.\d{1,2}\.\d{4,4}\s{1,1}\d{1,2}:\d{1,2}:\d{1,2}\s{1,1}';
my $accept_message = 'ERROR|WARN';

# ================
# Function`s scope
# ================

sub prepareMessage {
    my ($template, $error_full) = @_;

    if ($error_full =~ /$accept_message/i) {
	my $error_title = substr($error_full, 0, 30) . "...";
	$template =~ s/\$error-title/$error_title/gi;
	$template =~ s/\$error-full/$error_full/gi;
	
	return $template;
    }

    return "";
}

# ============
# Main program
# ============

open(TEMPLATE, "template.html") || die $!;
binmode(TEMPLATE, ":utf8");

open(INDEX, ">index.html") || die $!;
binmode(INDEX, ":utf8");

#
# Write header
#

while (<TEMPLATE>) {
    last if (/<!-- MESSAGE START -->/);
    print INDEX;
}

my $template = $_;

#
# Messages template
#
while (<TEMPLATE>) {
    $template = $template .  $_;
    last if (/<!-- MESSAGE END -->/);
}

foreach my $log (@log_files) {
    open(LOG, $log);
    binmode(LOG, ":utf8");

    my $message = "";
    while (<LOG>) {
	if (/$new_message/i) {
	    print INDEX prepareMessage($template, $message);
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
