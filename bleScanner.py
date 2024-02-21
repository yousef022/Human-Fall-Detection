######################################################
# Retrieves a the list of the device's service UUIDs #
######################################################

import asyncio
from bleak import BleakClient

DEVICE_ADDRESS = "ce:e4:05:be:70:cd"  # The specific device address
OUTPUT_FILE = "ble_output.txt"  # Output file name

async def connect_and_list_services(address):
    with open(OUTPUT_FILE, "w") as file:  # Open the file for writing
        try:
            async with BleakClient(address) as client:
                # Check if we are connected to the device
                if await client.is_connected():
                    file.write(f"Connected to {address}\n")
                    services = await client.get_services()
                    for service in services:
                        file.write(f"Service: {service.uuid}\n")
                        for char in service.characteristics:
                            file.write(f"   Characteristic: {char.uuid}\n")
                else:
                    file.write(f"Failed to connect to {address}\n")
        except Exception as e:
            file.write(f"An error occurred: {e}\n")

asyncio.run(connect_and_list_services(DEVICE_ADDRESS))