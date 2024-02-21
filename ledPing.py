##############################
# Tool to check connectivity #
##############################

import asyncio
from bleak import BleakClient

BEACON_ADDRESS = "ce:e4:05:be:70:cd"  # Replace with your beacon's MAC address
LED_CONTROL_CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"  # Replace with the actual characteristic UUID

async def turn_on_led():
    async with BleakClient(BEACON_ADDRESS) as client:
        connected = await client.is_connected()
        if connected:
            # The value to write depends on the beacon's specification
            await client.write_gatt_char(LED_CONTROL_CHARACTERISTIC_UUID, bytearray([0x01]))
            print("LED should now be on.")

asyncio.run(turn_on_led())