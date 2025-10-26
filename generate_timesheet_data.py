#!/usr/bin/env python3
"""
Generate Timesheet Data SQL Script
Tạo dữ liệu chấm công từ 01/04/2025 đến 26/10/2025
Cho 2 nhân viên: Hoàng Lan (ID=6), Đỗ Minh (ID=7)
"""

from datetime import datetime, timedelta
import random

# Configuration
START_DATE = datetime(2025, 4, 1)
END_DATE = datetime(2025, 10, 26)
STAFF_IDS = [6, 7]
STAFF_NAMES = {6: "Hoàng Lan", 7: "Đỗ Minh"}

# Working hours range
CHECK_IN_START = 8  # 8:00 AM
CHECK_IN_END = 9    # 9:00 AM
CHECK_OUT_START = 17  # 5:00 PM
CHECK_OUT_END = 18    # 6:00 PM

# Notes pool (10% chance to have a note)
NOTES_POOL = [
    "Họp team buổi sáng",
    "Làm thêm giờ",
    "Training nhân viên mới",
    "Kiểm kê kho",
    "Họp với khách hàng",
    None, None, None, None, None  # 50% chance of NULL
]

def is_working_day(date):
    """Check if date is a working day (Monday-Friday)"""
    return date.weekday() < 5  # 0=Monday, 4=Friday

def generate_check_in(date):
    """Generate random check-in time between 8:00-9:00"""
    hour = CHECK_IN_START
    minute = random.randint(0, 59)
    return date.replace(hour=hour, minute=minute, second=0)

def generate_check_out(check_in):
    """Generate check-out time (8-9 hours after check-in)"""
    # Add 8-9 hours
    hours_worked = random.uniform(8.0, 9.5)
    check_out = check_in + timedelta(hours=hours_worked)
    return check_out

def calculate_hours(check_in, check_out):
    """Calculate hours worked"""
    delta = check_out - check_in
    hours = delta.total_seconds() / 3600
    return round(hours, 2)

def generate_sql():
    """Generate SQL INSERT statements"""
    
    sql_lines = []
    sql_lines.append("-- " + "="*60)
    sql_lines.append("-- TIMESHEET DATA - GENERATED")
    sql_lines.append(f"-- From {START_DATE.date()} to {END_DATE.date()}")
    sql_lines.append("-- Staff: Hoàng Lan (ID=6), Đỗ Minh (ID=7)")
    sql_lines.append("-- " + "="*60)
    sql_lines.append("")
    sql_lines.append("BEGIN;")
    sql_lines.append("")
    
    # Track stats
    stats = {staff_id: {"days": 0, "hours": 0.0} for staff_id in STAFF_IDS}
    
    # Generate data
    current_date = START_DATE
    batch_count = 0
    values = []
    
    while current_date <= END_DATE:
        if is_working_day(current_date):
            # 95% chance of working (5% sick leave/vacation)
            if random.random() > 0.05:
                for staff_id in STAFF_IDS:
                    check_in = generate_check_in(current_date)
                    check_out = generate_check_out(check_in)
                    hours = calculate_hours(check_in, check_out)
                    note = random.choice(NOTES_POOL)
                    
                    # Format values
                    check_in_str = check_in.strftime("%Y-%m-%d %H:%M:%S")
                    check_out_str = check_out.strftime("%Y-%m-%d %H:%M:%S")
                    date_str = current_date.strftime("%Y-%m-%d")
                    note_str = f"'{note}'" if note else "NULL"
                    
                    value = f"({staff_id}, '{check_in_str}', '{check_out_str}', '{date_str}', {hours}, {note_str})"
                    values.append(value)
                    
                    # Update stats
                    stats[staff_id]["days"] += 1
                    stats[staff_id]["hours"] += hours
                    
                    # Insert in batches of 100
                    if len(values) >= 100:
                        sql_lines.append("INSERT INTO TimeSheets (staff_id, check_in, check_out, date, hours_worked, notes) VALUES")
                        sql_lines.append(",\n".join(values) + ";")
                        sql_lines.append("")
                        values = []
                        batch_count += 1
        
        current_date += timedelta(days=1)
    
    # Insert remaining values
    if values:
        sql_lines.append("INSERT INTO TimeSheets (staff_id, check_in, check_out, date, hours_worked, notes) VALUES")
        sql_lines.append(",\n".join(values) + ";")
        sql_lines.append("")
    
    # Add verification query
    sql_lines.append("-- Verify data")
    sql_lines.append("SELECT ")
    sql_lines.append("    u.firstname || ' ' || u.lastname as staff_name,")
    sql_lines.append("    COUNT(*) as total_days,")
    sql_lines.append("    ROUND(SUM(t.hours_worked), 2) as total_hours,")
    sql_lines.append("    ROUND(AVG(t.hours_worked), 2) as avg_hours_per_day")
    sql_lines.append("FROM TimeSheets t")
    sql_lines.append("JOIN Users u ON t.staff_id = u.id")
    sql_lines.append("WHERE t.date >= '2025-04-01' AND t.date <= '2025-10-26'")
    sql_lines.append("GROUP BY u.id, u.firstname, u.lastname")
    sql_lines.append("ORDER BY u.id;")
    sql_lines.append("")
    sql_lines.append("COMMIT;")
    sql_lines.append("")
    
    # Add stats comment
    sql_lines.append("-- " + "="*60)
    sql_lines.append("-- STATISTICS")
    sql_lines.append("-- " + "="*60)
    for staff_id in STAFF_IDS:
        name = STAFF_NAMES[staff_id]
        days = stats[staff_id]["days"]
        hours = round(stats[staff_id]["hours"], 2)
        avg = round(hours / days, 2) if days > 0 else 0
        sql_lines.append(f"-- {name} (ID={staff_id}):")
        sql_lines.append(f"--   Total days: {days}")
        sql_lines.append(f"--   Total hours: {hours}")
        sql_lines.append(f"--   Average hours/day: {avg}")
    sql_lines.append("-- " + "="*60)
    
    return "\n".join(sql_lines)

if __name__ == "__main__":
    sql = generate_sql()
    
    # Write to file
    output_file = "docker/init/06_timesheet_data.sql"
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql)
    
    print(f"✅ Generated timesheet data: {output_file}")
    print(f"📅 Date range: {START_DATE.date()} to {END_DATE.date()}")
    print(f"👥 Staff: {', '.join(STAFF_NAMES.values())}")
    print(f"📊 Run this script to generate fresh data anytime!")
