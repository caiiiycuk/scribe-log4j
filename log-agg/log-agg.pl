#!/usr/bin/perl -w

use strict;
use utf8;
use File::ReadBackwards;
use Digest::MD5 qw(md5_hex);
use File::Copy;

# =============
# Configuration
# =============

#
# Log-file to look for
#
my $log_file = '/opt/production/portal/tomcat1_old/logs/catalina.out';

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
# Count of messages to display, 0 - unlimeted
#
my $max_count = 50;

#
# Target file (index.html)
#
my $target = '/opt/WWW/https.w42.ru/logs/tomcat1.html';

#
# Template file
#
my $template_file = "template.html";

# ================
# Function`s scope
# ================

sub prepareMessage {
    my ($template, $error_full, $count) = @_;

    if ($error_full =~ $new_message) {
	my $time = "$1 ($count)";
	$template =~ s/\$error-time/$time/ig;
    }

    $template =~ s/\$error-full/$error_full/ig;
	
    return $template;
}

# ============
# Main program
# ============

#
# Read previus md5 if exsists
#
my $last_md5;

if (-e $target) {
    open(INDEX, "$target") || die $!;
    binmode(INDEX, ":utf8");

    $_ = <INDEX>;
    /<!--MD5:(.*)-->/i;
    $last_md5 = $1;
    
    close(INDEX);
}

#
# Read log files into messages hash
#
my $file = File::ReadBackwards->new("$log_file") || die "can't read file '$log_file': $!\n";

my %messages = ();
my $message = "";
my $md5;

while (defined($_ = $file->readline)) {
    $md5 = md5_hex($_) if (!$md5);
    last if ($last_md5 && md5_hex($_) eq $last_md5);

    $message = $_ . $message;

    if (/$new_message/i) {
	if ($message =~ /$accept_message/i) {
	    $messages{$message} = 0 if (!exists $messages{$message});
	    $messages{$message}++;

	    last if ($max_count && scalar(values(%messages)) > $max_count);
	}
	
	$message = "";
    }
}

$file->close();

#
# Make backup of previus $target
#
my $target_backup = "#";

if (-e $target) {
    if (scalar(values(%messages)) == 0) {
	#nothing to do (up to date)
	print "$target is up to date \n";
	exit 0;
    }
    
    $target_backup = $target;
    $target_backup =~ s/\.html$//i;
    $target_backup = $target_backup ."_". $last_md5 . ".html";

    print $target, "->", $target_backup, "\n";
    copy($target, $target_backup) or die "Copy failed: $!";

    $target_backup =~ s/^.*\///i;
}

#
# Open resources
#
open(TEMPLATE, "$template_file") || die $!;
binmode(TEMPLATE, ":utf8");

open(INDEX, ">$target") || die $!;
binmode(INDEX, ":utf8");

#
# Write md5 of position in log file
#
print INDEX "<!--MD5:$md5-->\n";

#
# Write header
#
while (<TEMPLATE>) {
    s/<!-- OLDER -->/$target_backup/ig;

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
    print INDEX prepareMessage($template, $message, $messages{$message});
}

#
# Write footer
#
while (<TEMPLATE>) {
    s/<!-- OLDER -->/$target_backup/ig;
    print INDEX;
}

#
# Close resources
#
close(INDEX);
close(TEMPLATE);

#
# Success info
#
print "$target was created\n";
