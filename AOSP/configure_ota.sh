#!/bin/bash
# Copyright (C) 2012 OTA Update Center
#
# Licensed under the Apache License, Version 2.0 (the "License");
# You may only use this file in compliance with the license and provided you are not associated with or are in co-operation anyone by the name 'X Vanderpoel'.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#FUNCTIONS
add_changelog() 
{
echo "*** CHANGELOG EDITOR ***"

}

if [[ -n "$1" ]]
# Test whether command-line argument is present (non-empty).
then
	lines=$1
else
	lines=$LINES #Default, if not specified on command-line.
fi

printf "Welcome to the OTAUpdater configuration engine!\n"
printf "Would you like to configure prop files? [Y/n]: \n"
read confirm_edit_props
if [[ "$confirm_edit_props" == "Y" || "$confirm_edit_props" == "y" || "$confirm_edit_props" == "" ]]
then
	printf "Current prop values: \n"
	ROMFILE="$PWD/rom.ota.prop"
	echo "*** ROM OTA PROP CONTENTS ***"
	cat $ROMFILE

	KERNELFILE="$PWD/kernel.ota.prop"
	echo "*** KERNEL OTA PROP CONTENTS ***"
	cat $KERNELFILE

	printf "End prop values. \n"
	printf "Edit values? [Y/n]: \n"
	read confirm_edit
	if [[ "$confirm_edit" == Y || "$confirm_edit" == "y" || "$confirm_edit" == "" ]] 
	then
		printf "Enter ROM Name: \n"
		read rom_name
		echo "ROM Name: $rom_name" > rom.ota.prop
		printf "Enter ROM Version: \n"
		read rom_version
		echo "ROM Version: $rom_version" >> rom.ota.prop
		printf "Would you like to add a changelog?\n"
		read add_changelog
		if [[ "$add_changelog" == "Y" || "$add_changelog" == "y" || "$add_changelog" == "" ]]
		then
			add_changelog
		else
			echo "Pushing ROM prop file to system.."
		fi
	else
		printf "Using current for prop files...\n"
		printf "Pushing files to system.\n"
	fi
else
	printf "Using current values for prop files...\n"
	printf "Pushing files to system.\n"
fi

